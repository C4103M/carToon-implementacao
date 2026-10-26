package com.cartoon.api.dev;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.repository.OficinaRepository;
import com.cartoon.api.ordemServico.models.Desconto;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.CalculadoraOrcamento;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.repositories.PecaRepository;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repository.ServicoRepository;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Profile({"dev", "default"})
@RequiredArgsConstructor
public class DadosIniciais implements ApplicationRunner {

    private final OficinaRepository oficinaRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PecaRepository pecaRepository;
    private final ServicoRepository servicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final CalculadoraOrcamento calculadoraOrcamento;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Oficina Matriz com margemPeca padrão
        Oficina matriz = oficinaRepository.findAll().stream().findFirst().orElseGet(() -> {
            Oficina o = new Oficina();
            o.setNome("Oficina Matriz");
            o.setEndereco("Rua Exemplo, 100");
            o.setTelefone("11999990000");
            o.setEmail("contato@oficinamatriz.com.br");
            o.setValorHora(new BigDecimal("100.00"));
            o.setTaxaRisco(new BigDecimal("0.10"));
            o.setMargemPeca(new BigDecimal("1.15"));
            o.setAtivo(true);
            return oficinaRepository.save(o);
        });

        if (matriz.getMargemPeca() == null || matriz.getMargemPeca().compareTo(BigDecimal.ONE) <= 0) {
            matriz.setMargemPeca(new BigDecimal("1.15"));
            matriz = oficinaRepository.save(matriz);
        }

        final Oficina oficinaMatriz = matriz;

        // 2. Usuários
        Usuario admin = criarOuAtualizarUsuario("Administrador", "admin@teste.com", "senha123", Role.ADMIN, oficinaMatriz);
        Usuario mecanico = criarOuAtualizarUsuario("Mecânico de Teste", "mecanico@teste.com", "senha123", Role.MECANICO, oficinaMatriz);
        criarOuAtualizarUsuario("Super Administrador", "superadmin@teste.com", "senha123", Role.SUPERADMIN, null);

        // 3. Clientes e Veículos
        Cliente cliente1 = clienteRepository.findByCpf("12345678901").orElseGet(() -> {
            Cliente c = new Cliente();
            c.setNome("Carlos Eduardo Santos");
            c.setCpf("12345678901");
            c.setTelefone("11999999999");
            c.setEndereco("Av. Paulista, 1000 - Bela Vista, São Paulo - SP");
            c.setOficina(oficinaMatriz);
            return clienteRepository.save(c);
        });

        Cliente cliente2 = clienteRepository.findByCpf("98765432100").orElseGet(() -> {
            Cliente c = new Cliente();
            c.setNome("Mariana Oliveira Costa");
            c.setCpf("98765432100");
            c.setTelefone("11988887777");
            c.setEndereco("Rua das Flores, 250 - Pinheiros, São Paulo - SP");
            c.setOficina(oficinaMatriz);
            return clienteRepository.save(c);
        });

        Cliente cliente3 = clienteRepository.findByCpf("45678912345").orElseGet(() -> {
            Cliente c = new Cliente();
            c.setNome("Roberto Santos Ferreira");
            c.setCpf("45678912345");
            c.setTelefone("11977776666");
            c.setEndereco("Rua Augusta, 450 - Consolação, São Paulo - SP");
            c.setOficina(oficinaMatriz);
            return clienteRepository.save(c);
        });

        Veiculo veiculoGol = veiculoRepository.findByPlacaIgnoreCase("ABC1D23").orElseGet(() -> {
            Veiculo v = new Veiculo();
            v.setPlaca("ABC1D23");
            v.setMarca("Volkswagen");
            v.setModelo("Gol 1.0 Flex");
            v.setAnoModelo(2020);
            v.setCombustivel("Flex");
            v.setCliente(cliente1);
            v.setValorFipe(new BigDecimal("50000.00"));
            return veiculoRepository.save(v);
        });

        Veiculo veiculoCivic = veiculoRepository.findByPlacaIgnoreCase("XYZ9K88").orElseGet(() -> {
            Veiculo v = new Veiculo();
            v.setPlaca("XYZ9K88");
            v.setMarca("Honda");
            v.setModelo("Civic Sedan EXL 2.0 Flex Aut.");
            v.setAnoModelo(2021);
            v.setCombustivel("Flex");
            v.setCliente(cliente2);
            v.setValorFipe(new BigDecimal("115000.00"));
            return veiculoRepository.save(v);
        });

        Veiculo veiculoStrada = veiculoRepository.findByPlacaIgnoreCase("BRA2E19").orElseGet(() -> {
            Veiculo v = new Veiculo();
            v.setPlaca("BRA2E19");
            v.setMarca("Fiat");
            v.setModelo("Strada Freedom 1.3 Flex");
            v.setAnoModelo(2022);
            v.setCombustivel("Flex");
            v.setCliente(cliente3);
            v.setValorFipe(new BigDecimal("78000.00"));
            return veiculoRepository.save(v);
        });

        // 4. Peças mockadas para a oficina matriz
        Peca pecaOleo5w30 = criarPecaSeNaoExistir("Óleo de Motor 5W30 Sintético", "Motul", new BigDecimal("52.00"), oficinaMatriz);
        Peca pecaOleo5w40 = criarPecaSeNaoExistir("Óleo de Motor 5W40 Sintético", "Castrol", new BigDecimal("58.00"), oficinaMatriz);
        Peca pecaFiltroOleo = criarPecaSeNaoExistir("Filtro de Óleo", "Fram", new BigDecimal("38.00"), oficinaMatriz);
        Peca pecaFiltroAr = criarPecaSeNaoExistir("Filtro de Ar do Motor", "Tecfil", new BigDecimal("42.00"), oficinaMatriz);
        Peca pecaFiltroComb = criarPecaSeNaoExistir("Filtro de Combustível", "Bosch", new BigDecimal("35.00"), oficinaMatriz);
        Peca pecaFiltroCabine = criarPecaSeNaoExistir("Filtro de Cabine / Ar Condicionado", "Mann Filter", new BigDecimal("45.00"), oficinaMatriz);
        Peca pecaPastilhaDiant = criarPecaSeNaoExistir("Jogo de Pastilhas de Freio Dianteiro", "Bosch", new BigDecimal("145.00"), oficinaMatriz);
        Peca pecaPastilhaTras = criarPecaSeNaoExistir("Jogo de Pastilhas de Freio Traseiro", "Fras-le", new BigDecimal("125.00"), oficinaMatriz);
        Peca pecaDiscosDiant = criarPecaSeNaoExistir("Par de Discos de Freio Ventilados", "Fremax", new BigDecimal("320.00"), oficinaMatriz);
        Peca pecaVelas = criarPecaSeNaoExistir("Jogo de Velas de Ignição Iridium", "NGK", new BigDecimal("180.00"), oficinaMatriz);
        Peca pecaCorreiaKit = criarPecaSeNaoExistir("Kit Correia Dentada e Tensor", "Continental", new BigDecimal("260.00"), oficinaMatriz);
        Peca pecaBombaAgua = criarPecaSeNaoExistir("Bomba d'Água do Motor", "Urba", new BigDecimal("175.00"), oficinaMatriz);
        Peca pecaAmortecedor = criarPecaSeNaoExistir("Amortecedor Dianteiro Pressurizado", "Monroe", new BigDecimal("290.00"), oficinaMatriz);
        Peca pecaAditivo = criarPecaSeNaoExistir("Aditivo Radiador Concentrado Orgânico", "Koube", new BigDecimal("36.00"), oficinaMatriz);
        Peca pecaBateria = criarPecaSeNaoExistir("Bateria 60Ah Selada", "Moura", new BigDecimal("440.00"), oficinaMatriz);
        Peca pecaFluidoFreio = criarPecaSeNaoExistir("Fluido de Freio DOT 4 500ml", "Varga", new BigDecimal("32.00"), oficinaMatriz);

        // 5. Serviços mockados para a oficina matriz
        Servico srvTrocaOleo = criarServicoSeNaoExistir(
                "Troca de Óleo e Filtro",
                LocalTime.of(0, 45),
                "Drenagem do óleo do cárter, substituição do filtro de óleo e reabastecimento do fluido conforme especificação.",
                oficinaMatriz
        );
        Servico srvAlinhamento = criarServicoSeNaoExistir(
                "Alinhamento e Balanceamento 3D",
                LocalTime.of(1, 0),
                "Alinhamento computadorizado da geometria de suspensão e balanceamento de precisão das 4 rodas.",
                oficinaMatriz
        );
        Servico srvFreios = criarServicoSeNaoExistir(
                "Revisão e Manutenção do Sistema de Freios",
                LocalTime.of(1, 30),
                "Inspeção completa de espessura de discos e pastilhas, sangria e troca de fluido, limpeza de pinças.",
                oficinaMatriz
        );
        Servico srvCorreia = criarServicoSeNaoExistir(
                "Substituição do Kit Correia Dentada e Bomba d'Água",
                LocalTime.of(2, 45),
                "Desmontagem dos periféricos da distribuição, troca da correia, tensor, bomba d'água e sincronismo do motor.",
                oficinaMatriz
        );
        Servico srvScanner = criarServicoSeNaoExistir(
                "Diagnóstico Eletrônico e Scanner OBD-II",
                LocalTime.of(0, 30),
                "Varredura completa de todas as ECUs do veículo, leitura de falhas ativas/passivas e reset de intervalos de manutenção.",
                oficinaMatriz
        );
        Servico srvBicos = criarServicoSeNaoExistir(
                "Limpeza e Equalização de Bicos Injetores",
                LocalTime.of(1, 15),
                "Descarbonização na cuba ultrassônica e teste de estanqueidade e vazão na bancada de testes.",
                oficinaMatriz
        );
        Servico srvArCondicionado = criarServicoSeNaoExistir(
                "Higienização do Ar Condicionado e Troca do Filtro",
                LocalTime.of(0, 40),
                "Aplicação de ozônio e produto bactericida nos dutos de ar e instalação de novo filtro de cabine.",
                oficinaMatriz
        );
        Servico srvRevisaoGeral = criarServicoSeNaoExistir(
                "Revisão Geral Preventiva 60 itens",
                LocalTime.of(3, 0),
                "Checagem detalhada de suspensão, freios, iluminação, correias, arrefecimento, ignição e teste de rodagem.",
                oficinaMatriz
        );

        // 6. Orçamentos / Ordens de Serviço mockados
        if (ordemServicoRepository.count() == 0) {
            // OS 1: PENDENTE (Orçamento recém-criado)
            OrdemServico os1 = new OrdemServico();
            os1.setOficina(oficinaMatriz);
            os1.setVeiculo(veiculoGol);
            os1.setMecanico(mecanico);
            os1.setDescricao("Revisão preventiva periódica de 50.000 km e troca de fluidos");
            os1.setStatusServico(StatusServico.PENDENTE);
            os1.setDesconto(Desconto.SEM_DESCONTO);
            os1.setDataOrcamento(LocalDate.now().minusDays(1));

            os1.adicionarItemServico(criarItemServico(srvTrocaOleo, 1, os1));
            os1.adicionarItemServico(criarItemServico(srvScanner, 1, os1));
            os1.adicionarItemPeca(criarItemPeca(pecaOleo5w30, 4, os1));
            os1.adicionarItemPeca(criarItemPeca(pecaFiltroOleo, 1, os1));
            os1.adicionarItemPeca(criarItemPeca(pecaFiltroAr, 1, os1));
            os1.recalcularTotal();
            ordemServicoRepository.save(os1);

            // OS 2: ACEITO (Orçamento aceito pelo cliente, aguardando início)
            OrdemServico os2 = new OrdemServico();
            os2.setOficina(oficinaMatriz);
            os2.setVeiculo(veiculoCivic);
            os2.setMecanico(mecanico);
            os2.setDescricao("Manutenção corretiva do sistema de freios dianteiros e sangria");
            os2.setStatusServico(StatusServico.ACEITO);
            os2.setDesconto(Desconto.CINCO_PORCENTO);
            os2.setDataOrcamento(LocalDate.now().minusDays(3));

            os2.adicionarItemServico(criarItemServico(srvFreios, 1, os2));
            os2.adicionarItemPeca(criarItemPeca(pecaPastilhaDiant, 1, os2));
            os2.adicionarItemPeca(criarItemPeca(pecaDiscosDiant, 1, os2));
            os2.adicionarItemPeca(criarItemPeca(pecaFluidoFreio, 2, os2));
            os2.recalcularTotal();
            ordemServicoRepository.save(os2);

            // OS 3: EM_ANDAMENTO (Serviço em execução pelo mecânico)
            OrdemServico os3 = new OrdemServico();
            os3.setOficina(oficinaMatriz);
            os3.setVeiculo(veiculoStrada);
            os3.setMecanico(mecanico);
            os3.setDescricao("Troca do kit de distribuição e revisão do sistema de arrefecimento");
            os3.setStatusServico(StatusServico.EM_ANDAMENTO);
            os3.setDesconto(Desconto.DEZ_PORCENTO);
            os3.setDataOrcamento(LocalDate.now().minusDays(2));
            os3.setDataInicio(LocalDate.now().minusDays(1));

            os3.adicionarItemServico(criarItemServico(srvCorreia, 1, os3));
            os3.adicionarItemServico(criarItemServico(srvBicos, 1, os3));
            os3.adicionarItemPeca(criarItemPeca(pecaCorreiaKit, 1, os3));
            os3.adicionarItemPeca(criarItemPeca(pecaBombaAgua, 1, os3));
            os3.adicionarItemPeca(criarItemPeca(pecaAditivo, 2, os3));
            os3.recalcularTotal();
            ordemServicoRepository.save(os3);

            // OS 4: FINALIZADO (Serviço concluído e faturado)
            OrdemServico os4 = new OrdemServico();
            os4.setOficina(oficinaMatriz);
            os4.setVeiculo(veiculoGol);
            os4.setMecanico(mecanico);
            os4.setDescricao("Alinhamento 3D, balanceamento completo e higienização de ar condicionado");
            os4.setStatusServico(StatusServico.FINALIZADO);
            os4.setDesconto(Desconto.CINCO_PORCENTO);
            os4.setDataOrcamento(LocalDate.now().minusDays(7));
            os4.setDataInicio(LocalDate.now().minusDays(6));
            os4.setDataFinalizacao(LocalDate.now().minusDays(5));

            os4.adicionarItemServico(criarItemServico(srvAlinhamento, 1, os4));
            os4.adicionarItemServico(criarItemServico(srvArCondicionado, 1, os4));
            os4.adicionarItemPeca(criarItemPeca(pecaFiltroCabine, 1, os4));
            os4.recalcularTotal();
            ordemServicoRepository.save(os4);
        }
    }

    private Usuario criarOuAtualizarUsuario(String nome, String email, String senha, Role role, Oficina oficina) {
        String emailNorm = email.trim().toLowerCase();
        return usuarioRepository.findByEmailIgnoreCase(emailNorm).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(emailNorm);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setRole(role);
            usuario.setOficina(oficina);
            usuario.setAtivo(true);
            return usuarioRepository.save(usuario);
        });
    }

    private Peca criarPecaSeNaoExistir(String nome, String fabricante, BigDecimal valorBase, Oficina oficina) {
        return pecaRepository.findAll().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome) && p.getFabricante().equalsIgnoreCase(fabricante))
                .findFirst()
                .orElseGet(() -> {
                    Peca p = new Peca();
                    p.setNome(nome);
                    p.setFabricante(fabricante);
                    p.setValorBase(valorBase);
                    p.setOficina(oficina);
                    return pecaRepository.save(p);
                });
    }

    private Servico criarServicoSeNaoExistir(String nome, LocalTime tempo, String descricao, Oficina oficina) {
        return servicoRepository.findAll().stream()
                .filter(s -> s.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElseGet(() -> {
                    Servico s = new Servico();
                    s.setNome(nome);
                    s.setTempo(tempo);
                    s.setDescricao(descricao);
                    s.setOficina(oficina);
                    return servicoRepository.save(s);
                });
    }

    private ItemServico criarItemServico(Servico servico, int quantidade, OrdemServico os) {
        ItemServico item = new ItemServico();
        item.setServico(servico);
        item.setQuantidade(quantidade);
        item.setTempo(servico.getTempo());
        BigDecimal valorUnitario = calculadoraOrcamento.valorServico(
                servico.getTempo(),
                os.getOficina().getValorHora(),
                os.getVeiculo().getValorFipe(),
                os.getOficina().getTaxaRisco()
        );
        item.setValorUnitario(valorUnitario);
        item.calcSubtotal();
        return item;
    }

    private ItemPeca criarItemPeca(Peca peca, int quantidade, OrdemServico os) {
        ItemPeca item = new ItemPeca();
        item.setPeca(peca);
        item.setQuantidade(quantidade);
        item.setValorBase(peca.getValorBase());
        BigDecimal margem = (os.getOficina().getMargemPeca() != null && os.getOficina().getMargemPeca().compareTo(BigDecimal.ONE) >= 0)
                ? os.getOficina().getMargemPeca()
                : BigDecimal.ONE;
        item.setMargem(margem);
        BigDecimal valorUnitario = calculadoraOrcamento.valorPeca(peca.getValorBase(), margem);
        item.setValorUnitario(valorUnitario);
        item.calcSubtotal();
        return item;
    }
}
