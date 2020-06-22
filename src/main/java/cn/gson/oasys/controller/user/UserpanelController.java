package cn.gson.oasys.controller.user;

import cn.gson.oasys.common.formValid.BindingResultVOUtil;
import cn.gson.oasys.common.formValid.MapToList;
import cn.gson.oasys.common.formValid.ResultEnum;
import cn.gson.oasys.common.formValid.ResultVO;
//import cn.gson.oasys.model.dao.informdao.InformRelationDao;
//import cn.gson.oasys.model.dao.maildao.MailreciverDao;
//import cn.gson.oasys.model.dao.processdao.NotepaperDao;
import cn.gson.oasys.model.dao.user.DeptDao;
import cn.gson.oasys.model.dao.user.PositionDao;
import cn.gson.oasys.model.dao.user.UserDao;
//import cn.gson.oasys.model.entity.mail.Mailreciver;
//import cn.gson.oasys.model.entity.notice.NoticeUserRelation;
//import cn.gson.oasys.model.entity.process.Notepaper;
import cn.gson.oasys.model.entity.user.User;
import cn.gson.oasys.services.user.NotepaperService;
import com.github.pagehelper.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.Soundbank;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/")
public class UserpanelController {
	@Autowired
	private UserDao udao;
	
	@Autowired
	private DeptDao ddao;
	@Autowired
	private PositionDao pdao;
	@Autowired
	private NotepaperService nservice;
	
	@Value("${img.rootpath}")
	private String rootpath;

	//@PostConstruct
	public void UserpanelController(){
		try {
			rootpath= ResourceUtils.getURL("classpath:").getPath().replace("/target/classes/","/static/image");

		}catch (IOException e){
			System.out.println("获取项目路径异常");
		}
	}

	@RequestMapping("userpanel")
	public String index(@SessionAttribute("userId") Long userId,Model model,HttpServletRequest req,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size){
		
		Pageable pa=new PageRequest(page, size);
		User user=null;
		if(!StringUtil.isEmpty((String) req.getAttribute("errormess"))){
			 user=(User) req.getAttribute("users");
			 req.setAttribute("errormess",req.getAttribute("errormess"));
		}
		else if(!StringUtil.isEmpty((String) req.getAttribute("success"))){
			user=(User) req.getAttribute("users"); 
			req.setAttribute("success","fds");
		}
		else{
			//找到这个用户
			user=udao.findOne(userId);
		}
		
		//找到部门名称
		String deptname=ddao.findname(user.getDept().getDeptId());
		
		//找到职位名称
		String positionname=pdao.findById(user.getPosition().getId());
		
		model.addAttribute("user", user);
		model.addAttribute("deptname", deptname);
		model.addAttribute("positionname", positionname);
		model.addAttribute("url", "panel");
		
	
		return "user/userpanel";
	}

	/**
	 * 修改用户
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	@RequestMapping("saveuser")
	public String saveemp(@RequestParam("filePath")MultipartFile filePath,HttpServletRequest request,@Valid User user,
			BindingResult br,@SessionAttribute("userId") Long userId) throws IllegalStateException, IOException{
		String imgpath=nservice.upload(filePath);
		User users=udao.findOne(userId);
		
		//重新set用户
		users.setRealName(user.getRealName());
		users.setUserTel(user.getUserTel());
		users.setEamil(user.getEamil());
		users.setAddress(user.getAddress());
		users.setUserEdu(user.getUserEdu());
		users.setSchool(user.getSchool());
		users.setIdCard(user.getIdCard());
		users.setBank(user.getBank());
		users.setSex(user.getSex());
		users.setThemeSkin(user.getThemeSkin());
		users.setBirth(user.getBirth());
		if(!StringUtil.isEmpty(user.getUserSign())){
			users.setUserSign(user.getUserSign());
		}
		if(!StringUtil.isEmpty(user.getPassword())){
			users.setPassword(user.getPassword());
		}
		if(!StringUtil.isEmpty(imgpath)){
			users.setImgPath(imgpath);
			
		}
		
		request.setAttribute("users", users);
		
		ResultVO res = BindingResultVOUtil.hasErrors(br);
		if (!ResultEnum.SUCCESS.getCode().equals(res.getCode())) {
			List<Object> list = new MapToList<>().mapToList(res.getData());
			request.setAttribute("errormess", list.get(0).toString());
		}else{
			udao.save(users);
			request.setAttribute("success", "执行成功！");
		}
		return "forward:/userpanel";
		
	}
	@RequestMapping("image/**")
	public void image(Model model, HttpServletResponse response, @SessionAttribute("userId") Long userId, HttpServletRequest request)
			throws Exception {
		String projectPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
		System.out.println(projectPath);
		String startpath = new String(URLDecoder.decode(request.getRequestURI(), "utf-8"));
		
		String path = startpath.replace("/image", "");
		
		File f = new File(rootpath, path);
		
		ServletOutputStream sos = response.getOutputStream();
		FileInputStream input = new FileInputStream(f.getPath());
		byte[] data = new byte[(int) f.length()];
		IOUtils.readFully(input, data);
		// 将文件流输出到浏览器
		IOUtils.write(data, sos);
		input.close();
		sos.close();
	}

}
