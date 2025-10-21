package nl.hva.ewa.regenwormen.domain;
import java.util.UUID;

public class Helpers {

    private Helpers() {
        // Private constructor om te voorkomen dat iemand dit object instantieert
    }

    /**
     * Genereert een korte hexadecimale ID gebaseerd op een UUID.
     * @param length De gewenste lengte (bijv. 4, 6 of 8)
     * @return Een korte unieke ID-string
     */
    public static String generateShortHexId(int length) {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, Math.min(length, 32)); // 32 is max zonder '-'
    }


}
