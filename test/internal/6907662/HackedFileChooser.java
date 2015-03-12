import javax.swing.*;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.plaf.ComponentUI;
import java.io.File;

public class HackedFileChooser extends JFileChooser {

    public static final Action NEW_FOLDER_ACTION =
            new HackedFileChooser().getNewFolderAction();

    public HackedFileChooser() {
        setUI(new HackedFileChooserUI());
        setCurrentDirectory(new File(System.getProperty("test.classes", ".")));
    }

    public Action getNewFolderAction() {
        return ((HackedFileChooserUI) getUI()).getNewFolderAction();
    }

    public void setUI(ComponentUI newUI) {
        super.setUI(newUI);
    }

    static class HackedFileChooserUI extends BasicFileChooserUI {
        HackedFileChooserUI() {
                super(null);
            }

            public Action getNewFolderAction() {
                return new NewFolderAction() {
                };
            }
    }

}
