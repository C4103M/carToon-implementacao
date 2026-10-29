import { useState } from 'react';
import { DECOUPLED_CATALOG, VEHICLES_DEMO } from '@/lib/landing-data';
import type { OrderLineItem, StatusServico } from '@/lib/landing-types';
import { formatCurrencyBRL } from '@/lib/landing-contact';

function buildInitialStatusMap(): Record<number, StatusServico> {
  return Object.fromEntries(
    VEHICLES_DEMO.map((v) => [v.id, v.osAtiva.statusInicial])
  );
}

function buildInitialItemsMap(): Record<number, OrderLineItem[]> {
  return Object.fromEntries(
    VEHICLES_DEMO.map((v) => [
      v.id,
      v.osAtiva.itensIniciais.map((item) => ({ ...item })),
    ])
  );
}

export function useOrderSimulation() {
  const [selectedVehicleIdx, setSelectedVehicleIdx] = useState(0);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [catalogOpen, setCatalogOpen] = useState(false);

  const [statusByVehicle, setStatusByVehicle] = useState<Record<number, StatusServico>>(
    buildInitialStatusMap
  );

  const [itemsByVehicle, setItemsByVehicle] = useState<Record<number, OrderLineItem[]>>(
    buildInitialItemsMap
  );

  const [catalogMultiplier, setCatalogMultiplier] = useState(1);
  const [notice, setNotice] = useState<string | null>(null);

  const currentVehicle = VEHICLES_DEMO[selectedVehicleIdx];
  const currentStatus = statusByVehicle[currentVehicle.id];
  const currentItems = itemsByVehicle[currentVehicle.id] || [];
  const isLocked = currentStatus === 'FINALIZADO';

  const totalPecas = currentItems
    .filter((i) => i.categoria === 'PECA')
    .reduce((acc, item) => acc + item.valorUnitarioCongelado * item.quantidade, 0);

  const totalServicos = currentItems
    .filter((i) => i.categoria === 'SERVICO')
    .reduce((acc, item) => acc + item.valorUnitarioCongelado * item.quantidade, 0);

  const totalOS = totalPecas + totalServicos;
  const fipePercentage = ((totalOS / currentVehicle.valorFipe) * 100).toFixed(1);
  const fipeBarWidth = Math.min(100, Math.max(6, (totalOS / currentVehicle.valorFipe) * 400));

  const selectVehicle = (index: number) => {
    setSelectedVehicleIdx(index);
    setHistoryOpen(false);
    setNotice(null);
  };

  const changeStatus = (nextStatus: StatusServico) => {
    setStatusByVehicle((prev) => ({
      ...prev,
      [currentVehicle.id]: nextStatus,
    }));
    if (nextStatus === 'FINALIZADO') {
      setCatalogOpen(false);
      setNotice('OS finalizada. Itens e valores estão travados e não podem ser alterados.');
    } else {
      setNotice(null);
    }
  };

  const addCatalogItem = (catalogId: string) => {
    if (isLocked) {
      setNotice('OS finalizada. Mude o status para "Em andamento" se quiser editar os itens.');
      return;
    }

    const found = DECOUPLED_CATALOG.find((c) => c.id === catalogId);
    if (!found) return;

    const unitPrice = Math.round(found.precoCatalogo * catalogMultiplier);

    setItemsByVehicle((prev) => {
      const existingList = prev[currentVehicle.id] || [];
      const existingIndex = existingList.findIndex(
        (i) => i.catalogId === catalogId && i.valorUnitarioCongelado === unitPrice
      );

      if (existingIndex >= 0) {
        const updated = [...existingList];
        updated[existingIndex] = {
          ...updated[existingIndex],
          quantidade: updated[existingIndex].quantidade + 1,
        };
        return { ...prev, [currentVehicle.id]: updated };
      }

      const newItem: OrderLineItem = {
        itemId: `sim-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        catalogId: found.id,
        nome: found.nome,
        categoria: found.categoria,
        quantidade: 1,
        valorUnitarioCongelado: unitPrice,
      };
      return { ...prev, [currentVehicle.id]: [...existingList, newItem] };
    });

    setNotice(
      `"${found.nome}" incluído por ${formatCurrencyBRL(unitPrice)} (preço congelado nesta OS).`
    );
  };

  const removeOrderItem = (itemId: string) => {
    if (isLocked) {
      setNotice('OS finalizada. Itens não podem mais ser removidos.');
      return;
    }

    setItemsByVehicle((prev) => ({
      ...prev,
      [currentVehicle.id]: (prev[currentVehicle.id] || []).filter(
        (item) => item.itemId !== itemId
      ),
    }));
  };

  const toggleCatalogInflation = () => {
    setCatalogMultiplier((prev) => {
      const next = prev === 1 ? 1.15 : 1;
      setNotice(
        next > 1
          ? 'Catálogo reajustado em +15%. Repare que os itens já lançados na OS mantiveram o valor antigo.'
          : 'Preços do catálogo restaurados ao valor original.'
      );
      return next;
    });
  };

  return {
    vehicles: VEHICLES_DEMO,
    catalog: DECOUPLED_CATALOG,
    currentVehicle,
    selectedVehicleIdx,
    currentStatus,
    currentItems,
    isLocked,
    totalPecas,
    totalServicos,
    totalOS,
    fipePercentage,
    fipeBarWidth,
    catalogMultiplier,
    notice,
    historyOpen,
    catalogOpen,
    setHistoryOpen,
    setCatalogOpen,
    setNotice,
    selectVehicle,
    changeStatus,
    addCatalogItem,
    removeOrderItem,
    toggleCatalogInflation,
  };
}

