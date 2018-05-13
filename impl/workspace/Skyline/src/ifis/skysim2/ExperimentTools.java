/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;


/**
 *
 * @author Christoph
 */
public class ExperimentTools {

    private static final String resultFolder = "result";

    /**
     * Returns a new file reference for a given base name, i.e. basename=Exp1  >> filename=result/Exp1_0
     * The tailing number is incremented everytime a new file is requested via this method (counter
     * is persistent on filesystem)
     * @param basename s.a.
     * @return s.a.
     * @throws IOException fail
     */
    public static  File getIncrementedOutFile(String basename) throws IOException {
        (new File("result")).mkdir();
        int result = 0;
        try {
            BufferedReader inC = new BufferedReader(new FileReader(resultFolder + "/" + basename + "_counter"));
            result = Integer.parseInt(inC.readLine());
            inC.close();
        } catch (Exception ex) {
            //
        }
        FileWriter outC = new FileWriter(resultFolder + "/" + basename + "_counter");
        outC.write(String.valueOf(result + 1));
        outC.close();
        return new File(resultFolder + "/" + basename + "_" + result+".txt");
    }

    /** @param filePath the name of the file to open. Not sure if it can accept URLs or just filenames. Path handling could be better, and buffer sizes are hardcoded
     */
    private static String readFileAsString(File file) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(file));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    static public void sendResultViaMail(File resultFile, String mailTo, boolean sendBody, boolean sendAttach) throws EmailException, IOException {
        String resultName = resultFile.getPath().replace('\\', '/').replaceAll(resultFolder + "/", "");
        // email
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName("vortex255.de");
        email.setAuthentication("web279p1", "freibad");
        email.addTo(mailTo, mailTo);
        email.setFrom("compute@ifis.cs.tu-bs.de", "Compute Server");
        email.setSubject("[C_RESULT] " + resultName);
        if (sendBody) {
            email.setMsg(readFileAsString(resultFile));
        } else {
            email.setMsg("See attachment.");
        }
        if (sendAttach) {
            // attachment
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath(resultFile.getPath());
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription(resultName);
            attachment.setName(resultName);
            email.attach(attachment);
        }

        email.send();
    }

}
