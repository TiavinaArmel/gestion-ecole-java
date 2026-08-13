import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class MainGestion extends JFrame {
// EN CAS D'ERREUR DU FICHIER JAR DU CONVERTISSEUR DE TXT EN PDF , VEUILLEZ AJUSTER LE BUILD PATH DU FICHIER JAR 
    private JTable table;
    private DefaultTableModel model;
    private GestionEtudiants gestion;
    private String classeActuelle;
    private JLabel statsLabel;

    public MainGestion() {
        // Créer le dossier data s'il n'existe pas
        new File("data").mkdirs();

        gestion = new GestionEtudiants();

        // Demande de la classe
        classeActuelle = JOptionPane.showInputDialog("Entrer la classe :");
        if (classeActuelle == null || classeActuelle.isEmpty()) {
            classeActuelle = "default";
        }

        // Chargement des données
        gestion.chargerEtudiants(getNomFichier());
        gestion.chargerNotes(getNomFichierNotes());

        // Fenêtre principale
        setTitle("Gestion - " + classeActuelle);
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== MENU =====
        // 8 boutons -> 8 lignes
        JPanel menu = new JPanel(new GridLayout(8, 1, 5, 5));

        JButton btnAjouter = new JButton("Ajouter étudiant");
        JButton btnSupprimer = new JButton("Supprimer étudiant");
        JButton btnModifier = new JButton("Modifier étudiant");
        JButton btnNotes = new JButton("Voir notes");
        JButton btnClassement = new JButton("Classement dans la classe");
        JButton btnRefresh = new JButton("Actualiser");
        JButton btnNotesMultiples = new JButton("ajouter les notes 1 etudiant");
        JButton btnPDF = new JButton("Exporter note en PDF");

        menu.add(btnAjouter);
        menu.add(btnSupprimer);
        menu.add(btnModifier);
        menu.add(btnNotes);
        menu.add(btnClassement);
        menu.add(btnRefresh);
        menu.add(btnNotesMultiples);
        menu.add(btnPDF);
        add(menu, BorderLayout.WEST);

        // Titre principal (utilisation de java.awt.Font)
        JLabel title = new JLabel("GESTION ETUDIANTS ET NOTES AVEC FICHIER DATA TXT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        // ===== TABLE NON ÉDITABLE =====
        model = new DefaultTableModel(
                new Object[]{"Matricule", "Nom", "Prénom", "Date", "Moyenne"}, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        // Centrer le contenu des colonnes (optionnel)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Panel central avec titre + tableau
        JPanel centerPanel = new JPanel(new BorderLayout());
        JLabel tableTitle = new JLabel("TABLEAU DES ETUDIANTS", SwingConstants.CENTER);
        tableTitle.setFont(new Font("Arial", Font.BOLD, 16));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tableTitle.setForeground(new Color(50, 50, 50));
        centerPanel.add(tableTitle, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ===== PANNEAU STATISTIQUES EN BAS =====
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsLabel = new JLabel();
        southPanel.add(statsLabel);
        add(southPanel, BorderLayout.SOUTH);

        // ===== ÉVÉNEMENTS =====
        btnAjouter.addActionListener(e -> ajouter());
        btnSupprimer.addActionListener(e -> supprimer());
        btnModifier.addActionListener(e -> modifier());
        btnNotes.addActionListener(e -> afficherNotes());
        btnClassement.addActionListener(e -> classement());
        btnRefresh.addActionListener(e -> refresh());
        btnNotesMultiples.addActionListener(e -> ajouterNotesMultiples());
        btnPDF.addActionListener(e -> exporterPDF());

        // Affichage initial
        refresh();
        updateStats();
    }

    // ===== NOMS DE FICHIERS =====
    private String getNomFichier() {
        return "data/" + classeActuelle.replace(" ", "_") + ".txt";
    }

    private String getNomFichierNotes() {
        return "data/" + classeActuelle.replace(" ", "_") + "_notes.txt";
    }

    // ===== AJOUTER UN ÉTUDIANT =====
    private void ajouter() {
        JTextField mat = new JTextField();
        JTextField nom = new JTextField();
        JTextField prenom = new JTextField();
        JTextField date = new JTextField();

        Object[] msg = {
                "Matricule:", mat,
                "Nom:", nom,
                "Prénom:", prenom,
                "Date:", date
        };

        if (JOptionPane.showConfirmDialog(this, msg) == JOptionPane.OK_OPTION) {
            Etudiant e = new Etudiant(
                    mat.getText(),
                    nom.getText(),
                    prenom.getText(),
                    date.getText(),
                    new Note()
            );
            gestion.ajouterEtudiant(e);
            save();
            refresh();
        }
    }

    // ===== SUPPRIMER =====
    private void supprimer() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String mat = model.getValueAt(row, 0).toString();
            gestion.supprimerUnEtudiantParMatricule(mat);
            save();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez un étudiant à supprimer !");
        }
    }

    // ===== MODIFIER =====
    private void modifier() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String mat = model.getValueAt(row, 0).toString();
            Etudiant e = gestion.rechercherEtudiantParMatricule(mat);
            if (e != null) {
                String nouveauNom = JOptionPane.showInputDialog(this, "Nom:", e.getNom());
                String nouveauPrenom = JOptionPane.showInputDialog(this, "Prénom:", e.getPrenom());
                String nouvelleDate = JOptionPane.showInputDialog(this, "Date de naissance:", e.getDate_Naissance());
                e.setNom(nouveauNom);
                e.setPrenom(nouveauPrenom);
                e.setDate_Naissance(nouvelleDate);
                save();
                refresh();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez un étudiant à modifier !");
        }
    }

    // ===== EXPORTER PDF (bulletin individuel) =====
    public void exporterBulletinPDF(Etudiant e) {
        try {
            String fichier = "data/bulletin_" + e.getMatricule() + ".pdf";

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(fichier));

            doc.open();

            // Utilisation du nom complet pour éviter le conflit avec java.awt.Font
            com.itextpdf.text.Font titreFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph titre = new Paragraph("BULLETIN SCOLAIRE", titreFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Nom : " + e.getNom()));
            doc.add(new Paragraph("Prénom : " + e.getPrenom()));
            doc.add(new Paragraph("Classe : " + classeActuelle));

            doc.add(new Paragraph(" "));

            // Tableau des notes
            PdfPTable pdfTable = new PdfPTable(3);
            pdfTable.addCell("Matière");
            pdfTable.addCell("Note");
            pdfTable.addCell("Coefficient");

            if (e.getNote() != null) {
                for (Map.Entry<String, Double> entry : e.getNote().getNotes().entrySet()) {
                    pdfTable.addCell(entry.getKey());
                    pdfTable.addCell(String.valueOf(entry.getValue()));
                    pdfTable.addCell("2"); // coefficient par défaut
                }
            }

            doc.add(pdfTable);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Moyenne : " + String.format("%.2f", e.getMoyenne())));

            doc.close();

            JOptionPane.showMessageDialog(this, "PDF généré : " + fichier);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération du PDF.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exporterPDF() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne un étudiant !");
            return;
        }

        String mat = model.getValueAt(row, 0).toString();
        Etudiant e = gestion.rechercherEtudiantParMatricule(mat);

        if (e != null) {
            exporterBulletinPDF(e);
        }
    }

    // FONCTION POUR AJOUTER NOTES MULTIPLES DE 9 MATIERE PAR DEFAUT
    private void ajouterNotesMultiples() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un étudiant !");
            return;
        }

        String matricule = model.getValueAt(row, 0).toString();
        Etudiant etudiant = gestion.rechercherEtudiantParMatricule(matricule);
        if (etudiant == null) return;

        // Création de la boîte de dialogue
        JDialog dialog = new JDialog(this, "Ajouter Notes", true);
        dialog.setSize(700, 400);
        dialog.setLayout(new BorderLayout());

        // Titre
        JLabel title = new JLabel("Notes : " + etudiant.getNom() + " " + etudiant.getPrenom(), SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        dialog.add(title, BorderLayout.NORTH);

        // Modèle de table avec 9 lignes par défaut
        String[] columns = {"Matière", "Note /20", "Coefficient"};
        DefaultTableModel notesModel = new DefaultTableModel(columns, 9) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        JTable notesTable = new JTable(notesModel);

        // Pré-remplir les coefficients avec 2
        for (int i = 0; i < notesModel.getRowCount(); i++) {
            notesModel.setValueAt("2", i, 2);
        }

        // Contrôle de saisie pour le coefficient (minimum 2)
        notesTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String valeur = getCellEditorValue().toString();
                    int coef = Integer.parseInt(valeur);
                    if (coef < 2) {
                        JOptionPane.showMessageDialog(dialog, "Le coefficient doit être au moins 2 !");
                        return false;
                    }
                    return super.stopCellEditing();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Veuillez saisir un nombre entier valide.");
                    return false;
                }
            }
        });

        dialog.add(new JScrollPane(notesTable), BorderLayout.CENTER);

        // Bouton Enregistrer
        JButton btnSave = new JButton("Enregistrer");
        btnSave.addActionListener(ev -> {
            for (int i = 0; i < notesModel.getRowCount(); i++) {
                String matiere = (String) notesModel.getValueAt(i, 0);
                String noteStr = (String) notesModel.getValueAt(i, 1);
                String coefStr = (String) notesModel.getValueAt(i, 2);

                if (matiere == null || matiere.trim().isEmpty()) continue; // ignorer les lignes sans matière
                if (noteStr == null || coefStr == null) continue;

                try {
                    double note = Double.parseDouble(noteStr.trim());
                    int coef = Integer.parseInt(coefStr.trim());

                    if (note < 0 || note > 20) {
                        JOptionPane.showMessageDialog(dialog, "La note doit être entre 0 et 20 !");
                        return;
                    }

                    // Ajouter la note (le coefficient n'est pas encore utilisé)
                    etudiant.getNote().ajouter(matiere.trim(), note);

                    // Si vous souhaitez gérer les coefficients, modifiez la classe Note en conséquence

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur de format sur la ligne " + (i+1));
                    return;
                }
            }
            save();    // méthode qui sauvegarde dans les fichiers
            refresh(); // rafraîchit le tableau principal
            dialog.dispose();
        });

        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    // ===== AFFICHER LES NOTES D'UN ÉTUDIANT =====
    private void afficherNotes() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un étudiant !");
            return;
        }

        String mat = model.getValueAt(row, 0).toString();
        Etudiant e = gestion.rechercherEtudiantParMatricule(mat);
        if (e == null) return;

        Note note = e.getNote();
        if (note == null || note.getNotes().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune note pour cet étudiant.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Notes de ").append(e.getPrenom()).append(" ").append(e.getNom()).append(" :\n\n");
        for (Map.Entry<String, Double> entry : note.getNotes().entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        sb.append("\nMoyenne : ").append(String.format("%.2f", e.getMoyenne()));

        JOptionPane.showMessageDialog(this, sb.toString(), "Notes de l'étudiant", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== CLASSEMENT =====
    private void classement() {
        gestion.trierParMoyenne();
        refresh();

        ArrayList<Etudiant> liste = gestion.getListeEtudiant();
        if (liste.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun étudiant dans la classe.");
            return;
        }

        StringBuilder sb = new StringBuilder("🏆 Top 3 de la classe 🏆\n\n");
        int nb = Math.min(3, liste.size());
        for (int i = 0; i < nb; i++) {
            Etudiant e = liste.get(i);
            sb.append(i + 1).append(". ")
                    .append(e.getPrenom()).append(" ").append(e.getNom())
                    .append(" – Moyenne : ").append(String.format("%.2f", e.getMoyenne()))
                    .append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Classement", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== RAFRAÎCHIR LE TABLEAU =====
    private void refresh() {
        model.setRowCount(0);
        for (Etudiant e : gestion.getListeEtudiant()) {
            model.addRow(new Object[]{
                    e.getMatricule(),
                    e.getNom(),
                    e.getPrenom(),
                    e.getDate_Naissance(),
                    String.format("%.2f", e.getMoyenne()) // format à 2 décimales
            });
        }
        updateStats();
    }

    // ===== SAUVEGARDER =====
    private void save() {
        gestion.sauvegarderEtudiants(getNomFichier());
        gestion.sauvegarderNotes(getNomFichierNotes());
    }

    // ===== METTRE À JOUR LES STATISTIQUES =====
    private void updateStats() {
        int nb = gestion.getListeEtudiant().size();
        double sommeMoy = 0.0;
        for (Etudiant e : gestion.getListeEtudiant()) {
            sommeMoy += e.getMoyenne();
        }
        double moyenneClasse = nb > 0 ? sommeMoy / nb : 0.0;
        statsLabel.setText(String.format("Classe: %s | Étudiants: %d | Moyenne générale: %.2f",
                classeActuelle, nb, moyenneClasse));
    }

    // ===== POINT D'ENTRÉE =====
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainGestion().setVisible(true));
    }
}