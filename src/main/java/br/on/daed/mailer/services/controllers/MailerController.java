package br.on.daed.mailer.services.controllers;

import br.on.daed.mailer.services.Mail;
import br.on.daed.mailer.services.Mailer;
import br.on.daed.mailer.services.contas.Conta;
import br.on.daed.mailer.services.contas.ContaDLO;
import br.on.daed.mailer.services.contas.tags.ContaTag;
import br.on.daed.mailer.services.contas.tags.ContaTagDLO;
import br.on.daed.mailer.services.jobs.EmailClick;
import br.on.daed.mailer.services.jobs.EmailLink;
import br.on.daed.mailer.services.jobs.Job;
import br.on.daed.mailer.services.jobs.JobDLO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author csiqueira
 */
@RequestMapping("/")
@Controller
public class MailerController {

	@Autowired
	private ContaTagDLO contaTagDLO;
	
    @Autowired
    private Mailer mailer;

    @Autowired
    private ContaDLO contaDLO;

    @Autowired
    private JobDLO jobDLO;

    @Autowired
    private HttpServletRequest request;

    final private String DEFAULT_REMETENTE = "nao-responder@on.br";
    final private String DEFAULT_SENHA = "";

    final public static String REQUEST_MAPPING_URL = "url";

    final public static int PAGE_SIZE = 15;

    private void setPagination(ModelMap map, String title, String url, Page page) {
        int current = page.getNumber() + 1;
        int begin = Math.max(1, current - 5);
        int end = Math.min(begin + 10, page.getTotalPages());

        map.addAttribute("paginationTitle", title);
        map.addAttribute("paginationUrl", url);

        map.addAttribute("page", page);
        map.addAttribute("beginIndex", begin);
        map.addAttribute("endIndex", end);
        map.addAttribute("currentIndex", current);

        map.addAttribute("queryString", request.getQueryString());

        map.addAttribute("paginationSize", PAGE_SIZE);

        map.addAttribute("pagina", "pagination");
    }

    @RequestMapping(value = "/click-list")
    public String getClickList(ModelMap map) {
        return getClickList(1, null, null, map);
    }

    @RequestMapping(value = "/click-list/{pageNumber}", method = RequestMethod.GET)
    public String getClickList(@PathVariable Integer pageNumber, @RequestParam(required = false) String email, @RequestParam(required = false) String url, ModelMap map) {
        Page<EmailClick> page = jobDLO.getClickLog(pageNumber, email, url);
        setPagination(map, "Clicks efetuados", "click-list", page);
        return "index";
    }

    @RequestMapping(value = "/link-list")
    public String getLinkList(ModelMap map) {
        return getLinkList(1, null, null, map);
    }

    @RequestMapping(value = "/link-list/{pageNumber}", method = RequestMethod.GET)
    public String getLinkList(@PathVariable Integer pageNumber, @RequestParam(required = false) String url, @RequestParam(required = false) String assunto, ModelMap map) {
        Page<EmailLink> page = jobDLO.getLinkLog(pageNumber, assunto, url);
        setPagination(map, "Links gerados", "link-list", page);
        return "index";
    }

    @RequestMapping(value = "/conta-list")
    public String getContaList(ModelMap map) {
        return getContaList(1, null, map);
    }

    @RequestMapping(value = "/conta-list/{pageNumber}", method = RequestMethod.GET)
    public String getContaList(@PathVariable Integer pageNumber, @RequestParam(required = false) String email, ModelMap map) {
        Page<Conta> page = contaDLO.getContaLog(pageNumber, email);
        setPagination(map, "Emails cadastrados", "conta-list", page);
        return "index";
    }

    @RequestMapping(value = "/job-list")
    public String getJobList(ModelMap map) {
        return getJobList(1, null, map);
    }

    @RequestMapping(value = "/job-list/{pageNumber}", method = RequestMethod.GET)
    public String getJobList(@PathVariable Integer pageNumber, @RequestParam(required = false) String assunto, ModelMap map) {
        Page<Job> page = jobDLO.getJobLog(pageNumber, assunto);
        setPagination(map, "Jobs", "job-list", page);
        return "index";
    }

    @RequestMapping(value = "/" + REQUEST_MAPPING_URL, method = RequestMethod.GET)
    public String url(@RequestParam("d") String data) throws IOException {
        String url = jobDLO.persistUserClick(data);
        return "redirect:" + url;
    }

    @RequestMapping("/estatisticas")
    public String getEstatisticas(ModelMap map) {

        contaDLO.indexStats(map);
        jobDLO.indexStats(map);

        map.addAttribute("pagina", "estatisticas");

        return "index";
    }

    @RequestMapping("/carregar-base-dados")
    public String getCarregarBaseDados(ModelMap map) {
        map.addAttribute("pagina", "carregar-base-dados");
		
		Iterable<ContaTag> tags = contaTagDLO.findAll();
		
		map.addAttribute("tags", tags);
		
        return "index";
    }

    @RequestMapping("/email-teste")
    public String getEmailTeste(ModelMap map) {
        map.addAttribute("pagina", "email-teste");
        return "index";
    }

    @RequestMapping("/email-em-massa")
    public String getEmailMassa(ModelMap map) {
        map.addAttribute("pagina", "email-em-massa");
        return "index";
    }

    @RequestMapping("/")
    public String index(ModelMap map) throws IOException {
        return "redirect:estatisticas";
    }

    @RequestMapping(value = "/received-mail-list", method = RequestMethod.POST)
    public @ResponseBody
    Boolean receiveMailList(@RequestParam("uuid") String uuid) throws UnsupportedOperationException, IOException {
        //MailSender.enablePerform(uuid);
        return true;
    }

    @RequestMapping(value = "/send-debug-mail-list", method = RequestMethod.POST)
    public @ResponseBody
    String sendTestMail(
            @RequestParam("destino") String destino,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("assunto") String assunto) throws IOException {

        String ret = "null";

        try {

            File arquivoCorpo = new File(System.getProperty("java.io.tmpdir") + File.separator + "corpo" + System.currentTimeMillis());
            arquivo.transferTo(arquivoCorpo);

            byte[] encoded = Files.readAllBytes(Paths.get(arquivoCorpo.getAbsolutePath()));
            String HTML = new String(encoded, "UTF-8");

            Mail m = mailer.criarMail(destino, DEFAULT_REMETENTE, DEFAULT_SENHA, HTML, assunto);

            Job createdJob = jobDLO.createJob(m, true);

            jobDLO.replaceLinksAndSend(m, createdJob);

            jobDLO.terminateJob(createdJob);

            ret = "true";
        } catch (Exception e) {
            ret = "false";
            e.printStackTrace();
        }

        return iframeReturn(ret, "alertaEmailTeste");
    }

    @RequestMapping(value = "/send-mail-list", method = RequestMethod.POST)
    public @ResponseBody
    String sendMailList(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("assunto") String assunto) throws IOException {

        String ret = "null";

        try {
            File arquivoCorpo = new File(System.getProperty("java.io.tmpdir") + File.separator + "corpo" + System.currentTimeMillis());
            arquivo.transferTo(arquivoCorpo);
            Mail m = mailer.criarMail(DEFAULT_REMETENTE, DEFAULT_SENHA, arquivoCorpo.getAbsolutePath(), assunto);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return iframeReturn(ret, "alertaEmailMassa");
    }

    @RequestMapping(value = "/load-mail-list", method = RequestMethod.POST)
    public @ResponseBody
    String loadMailList(@RequestParam("arquivo") final MultipartFile arquivo, @RequestParam("tags") String tags) throws IOException {

        String ret = "null";

        try {
            File arquivoCorpo = new File(System.getProperty("java.io.tmpdir") + File.separator + "corpo" + System.currentTimeMillis());
            arquivoCorpo.deleteOnExit();
            arquivo.transferTo(arquivoCorpo);
            List<String> listaEmail = Files.readAllLines(Paths.get(arquivoCorpo.getAbsolutePath()));
            ret = contaDLO.inserirEmails(listaEmail, tags).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return iframeReturn(ret, "alertaCarregarBaseDeDados");
    }

    private String iframeReturn(String val, String funcao) {
        return "<div id='retorno'>" + val + "</div><script>window.parent.window." + funcao + "(document.getElementById('retorno').innerHTML);</script>";
    }
}
