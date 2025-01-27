import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class ResumeApp extends JFrame implements ActionListener {

    private JComboBox<String> templateList, themeList;
    private JTextField fullName, contactNumber, emailId, personalSummary;
    private JTextArea workExperience, academicDetails, skillSet;
    private JButton generateBtn, saveBtn, uploadPicBtn, exportPdfBtn;
    private JLabel picturePreview;
    private JFileChooser fileChooser;

    private Map<String, String> templatePaths;
    private File profilePicture;

    public ResumeApp() {
        super("Advanced Resume Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        templatePaths=new HashMap<>();
        templatePaths.put("Modern","path/to/modern_template.html");
        templatePaths.put("Classic","path/to/classic_template.html");
        templatePaths.put("Creative","path/to/creative_template.html");

        JPanel topPanel=new JPanel(new GridLayout(3,3));
        topPanel.add(new JLabel("Select Template:"));
        templateList=new JComboBox<>(templatePaths.keySet().toArray(new String[0]));
        topPanel.add(templateList);
        topPanel.add(new JLabel("Theme:"));
        themeList=new JComboBox<>(new String[]{"Blue","Green","Red","Default"});
        topPanel.add(themeList);
        topPanel.add(new JLabel("Name:"));
        fullName=new JTextField(20);
        topPanel.add(fullName);
        topPanel.add(new JLabel("Contact:"));
        contactNumber=new JTextField(20);
        topPanel.add(contactNumber);
        topPanel.add(new JLabel("Email:"));
        emailId=new JTextField(20);
        topPanel.add(emailId);
        topPanel.add(new JLabel("Summary:"));
        personalSummary=new JTextField(20);
        topPanel.add(personalSummary);

        JPanel midPanel=new JPanel(new GridLayout(4,2));
        midPanel.add(new JLabel("Experience:"));
        workExperience=new JTextArea(5,20);
        midPanel.add(new JScrollPane(workExperience));
        midPanel.add(new JLabel("Education:"));
        academicDetails=new JTextArea(5,20);
        midPanel.add(new JScrollPane(academicDetails));
        midPanel.add(new JLabel("Skills:"));
        skillSet=new JTextArea(5,20);
        midPanel.add(new JScrollPane(skillSet));
        midPanel.add(new JLabel("Profile Picture:"));
        uploadPicBtn=new JButton("Upload Picture");
        picturePreview=new JLabel("No Picture Selected",SwingConstants.CENTER);
        picturePreview.setPreferredSize(new Dimension(150,150));
        midPanel.add(uploadPicBtn);
        midPanel.add(picturePreview);

        JPanel bottomPanel=new JPanel();
        generateBtn=new JButton("Generate");
        saveBtn=new JButton("Save");
        exportPdfBtn=new JButton("Export as PDF");
        bottomPanel.add(generateBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(exportPdfBtn);

        fileChooser=new JFileChooser();

        add(topPanel,BorderLayout.NORTH);
        add(midPanel,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);

        generateBtn.addActionListener(this);
        saveBtn.addActionListener(this);
        uploadPicBtn.addActionListener(this);
        exportPdfBtn.addActionListener(this);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if(event.getSource()==generateBtn) {
            createResumePreview();
        } else if(event.getSource()==saveBtn) {
            saveResumeToFile();
        } else if(event.getSource()==uploadPicBtn) {
            uploadProfilePicture();
        } else if(event.getSource()==exportPdfBtn) {
            exportResumeAsPdf();
        }
    }

    private void createResumePreview() {
        String template=(String)templateList.getSelectedItem();
        String path=templatePaths.get(template);
        if(path==null) {
            showError("Template not found.");
            return;
        }

        try {
            String resumeData=loadTemplate(path);
            resumeData=resumeData
                .replace("{FULL_NAME}",fullName.getText())
                .replace("{PHONE_NUMBER}",contactNumber.getText())
                .replace("{EMAIL}",emailId.getText())
                .replace("{SUMMARY}",personalSummary.getText())
                .replace("{EXPERIENCE}",workExperience.getText())
                .replace("{EDUCATION}",academicDetails.getText())
                .replace("{SKILLS}",skillSet.getText());

            JOptionPane.showMessageDialog(this,resumeData,"Preview",JOptionPane.INFORMATION_MESSAGE);

        } catch(IOException ex) {
            showError("Error loading template: "+ex.getMessage());
        }
    }

    private void saveResumeToFile() {
        int result=fileChooser.showSaveDialog(this);
        if(result==JFileChooser.APPROVE_OPTION) {
            File file=fileChooser.getSelectedFile();
            try {
                FileWriter writer=new FileWriter(file);
                writer.write(generateResumeContent());
                writer.close();
                JOptionPane.showMessageDialog(this,"Resume saved successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
            } catch(IOException ex) {
                showError("Error saving resume: "+ex.getMessage());
            }
        }
    }

    private void uploadProfilePicture() {
        int result=fileChooser.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION) {
            profilePicture=fileChooser.getSelectedFile();
            ImageIcon icon=new ImageIcon(new ImageIcon(profilePicture.getAbsolutePath()).getImage().getScaledInstance(150,150,Image.SCALE_SMOOTH));
            picturePreview.setIcon(icon);
            picturePreview.setText(null);
        }
    }

    private void exportResumeAsPdf() {
        int result=fileChooser.showSaveDialog(this);
        if(result==JFileChooser.APPROVE_OPTION) {
            File file=fileChooser.getSelectedFile();
            try {
                Document pdf=new Document();
                PdfWriter.getInstance(pdf,new FileOutputStream(file));
                pdf.open();
                pdf.add(new Paragraph("Resume"));
                pdf.add(new Paragraph("Name: "+fullName.getText()));
                pdf.add(new Paragraph("Contact: "+contactNumber.getText()));
                pdf.add(new Paragraph("Email: "+emailId.getText()));
                pdf.add(new Paragraph("Summary: "+personalSummary.getText()));
                pdf.add(new Paragraph("Experience: "+workExperience.getText()));
                pdf.add(new Paragraph("Education: "+academicDetails.getText()));
                pdf.add(new Paragraph("Skills: "+skillSet.getText()));
                if(profilePicture!=null) {
                    Image img=Image.getInstance(profilePicture.getAbsolutePath());
                    img.scaleToFit(150,150);
                    pdf.add(img);
                }
                pdf.close();
                JOptionPane.showMessageDialog(this,"PDF exported successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
            } catch(Exception ex) {
                showError("Error exporting PDF: "+ex.getMessage());
            }
        }
    }

    private String loadTemplate(String path) throws IOException {
        BufferedReader reader=new BufferedReader(new FileReader(path));
        StringBuilder content=new StringBuilder();
        String line;
        while((line=reader.readLine())!=null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    private String generateResumeContent() {
        return "Name: "+fullName.getText()+"\n"+
               "Contact: "+contactNumber.getText()+"\n"+
               "Email: "+emailId.getText()+"\n"+
               "Summary: "+personalSummary.getText()+"\n"+
               "Experience: "+workExperience.getText()+"\n"+
               "Education: "+academicDetails.getText()+"\n"+
               "Skills: "+skillSet.getText();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,message,"Error",JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ResumeApp::new);
    }
}
