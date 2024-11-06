package app.client.util;

import java.util.List;
import java.util.stream.Collectors;

import app.client.model.Media;
import app.client.model.UserMedia;


public class Stats {

    public static double calcularMedia(List<Double> valores) {
        return valores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static double calcularDesvioPadrao(List<Double> valores) {
        double media = calcularMedia(valores);
        double variancia = valores.stream()
                .mapToDouble(v -> Math.pow(v - media, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variancia);
    }

    public static long contarElementos(List<?> elementos) {
        return elementos.size();
    }

    public static String agruparPorMediaEUsuarios(List<Media> medias, List<UserMedia> userMedia) {
        return medias.stream()
                .map(media -> {
                    long numUsuarios = userMedia.stream()
                            .filter(um -> um.getMediaId().equals(media.getId()))  
                            .count();
                    return "Media: " + media.getTitle() + ", Número de Usuários: " + numUsuarios;
                })
                .collect(Collectors.joining("\n"));  //ver se n há problema!!!!!!
    }
}
