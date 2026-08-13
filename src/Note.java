import java.util.HashMap;
import java.util.Map;

public class Note {
    private Map<String, Double> notes;

    public Note() {
        notes = new HashMap<>();
    }

    public void ajouter(String matiere, double valeur) {
        notes.put(matiere, valeur);
    }

    public Map<String, Double> getNotes() {
        return notes;
    }

    public double getMoyenne() {
        if (notes.isEmpty()) return 0.0;
        double somme = 0.0;
        for (double v : notes.values()) {
            somme += v;
        }
        return somme / notes.size();
    }
}