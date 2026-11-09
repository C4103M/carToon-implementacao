package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.Granularidade;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

final class SerieTemporal {

    private SerieTemporal() {
    }

    static List<String> chaves(LocalDate inicio, LocalDate fim, Granularidade granularidade) {
        List<String> chaves = new ArrayList<>();
        if (granularidade == Granularidade.DIA) {
            for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
                chaves.add(d.toString());
            }
        } else {
            for (YearMonth m = YearMonth.from(inicio); !m.isAfter(YearMonth.from(fim)); m = m.plusMonths(1)) {
                chaves.add(m.toString());
            }
        }
        return chaves;
    }

    static String chave(LocalDate data, Granularidade granularidade) {
        if (data == null) {
            return "";
        }
        return granularidade == Granularidade.DIA ? data.toString() : YearMonth.from(data).toString();
    }
}
