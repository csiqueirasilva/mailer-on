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

//    private final static Map<String, Mail> tasks = new HashMap();
//    private final static Map<String, Boolean> canPerform = new HashMap();
//    public final static String MESSAGE_CHANNEL = "/message/";
//    public final static String TERMINADO_CHANNEL = "/finished/";
//
//    private @Autowired
//    SimpMessagingTemplate template;
//
//    public static void enablePerform(String uuid) {
//        if (tasks.containsKey(uuid)) {
//            canPerform.put(uuid, true);
//        }
//    }
//
//    public static String criarTarefaEnvioEmail(Mail m) {
//        String uuid = UUID.randomUUID().toString();
//        tasks.put(uuid, m);
//        canPerform.put(uuid, false);
//        return uuid;
//    }
//
//    @Scheduled(fixedDelay = 10)
//    public void sendMail() {
//        if (tasks.size() > 0) {
//            for (String taskId : tasks.keySet()) {
//                if (canPerform.get(taskId)) {
//                    Mail m = tasks.get(taskId);
//                    if (m.getTo().size() > 0) {
//                        String to = m.getTo().get(0);
//                        m.getTo().remove(0);
//                        String ret = "Email enviado para " + to;
//                        try {
//                            Mailer.sendEmail(m.getUser(), m.getPassword(), m.getSubject(), m.getBody(), to);
//                        } catch (Exception e) {
//                            ret = "Falha ao enviar email para " + to + " " + e.getMessage();
//                        }
//
//                        m.incrementarEnviado();
//
//                        template.convertAndSend(MESSAGE_CHANNEL + taskId, m.getEnviados() + "/" + m.getTotal() + " - " + ret);
//                    }
//
//                    if (m.getTo().isEmpty()) {
//                        tasks.remove(taskId);
//                        canPerform.remove(taskId);
//                        template.convertAndSend(TERMINADO_CHANNEL + taskId, "Tarefa concluída");
//                    }
//                }
//            }
//        }
//    }
}
