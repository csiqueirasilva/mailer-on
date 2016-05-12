/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.tasks;

import br.on.daed.mailer.services.jobs.Job;
import br.on.daed.mailer.services.jobs.JobDLO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author csiqueira
 */
@EnableScheduling
@Service
public class MailSender {

    private static Job workingJob = null;
    private static boolean working = false;
    private static boolean error = false;
    private static String lastErrorMessage = null;

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static Job getWorkingJob() {
        return workingJob;
    }

    @Autowired
    private JobDLO jobDLO;

    @Scheduled(fixedDelay = 500)
    public void sendMail() {
        if (!error) {
            if (workingJob == null) {
                workingJob = jobDLO.getCurrentJob();
            } else if (!working) {
                working = true;

                try {
                    if (jobDLO.work(workingJob)) {
                        workingJob = null;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, ex);
                    lastErrorMessage = ex.getMessage();
                    error = true;
                }

                working = false;
            }
        }
    }
}
