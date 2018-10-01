package com.jade.swp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jade.swp.util.FileUtils;

@Controller
public class UploadController {

	private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	@Resource(name = "uploadPath")
	private String uploadPath;

	@RequestMapping(value = "/uploadForm", method = RequestMethod.GET)
	public void uploadFormGET() throws Exception {
		logger.info("upload GET .....");
	}
	
	@ResponseBody
	@RequestMapping(value = "/uploadAjax", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> uploadFormAJAX(MultipartFile file, String type) throws Exception {
		logger.info("upload AJAX .....orinalName={}, size={}, contentType={}", 
				file.getOriginalFilename(),
				file.getSize(),
				file.getContentType());
		logger.info("......type={}", type);
		
		try {
			String savedFileName = FileUtils.uploadFile(file, uploadPath);
			return new ResponseEntity<>(savedFileName, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/deleteFile", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteFile(String fileName) throws Exception {
		logger.info("deleteFile.....fileName={}", fileName);
		
		try {
			boolean isImage = FileUtils.getMediaType(FileUtils.getFileExtension(fileName)) != null;
			File file = new File(uploadPath + fileName);
			file.delete();
			
			// image면 원본 이미지도 삭제!
			if (isImage) {
				// /2018/09/21/s_resaldsfjadfldsj_realname.jpg
				int lastSlash = fileName.lastIndexOf("/") + 1;
				String realName = fileName.substring(0, lastSlash) + fileName.substring(lastSlash + 2);
				File real = new File(uploadPath + realName);
				real.delete();
			}
			
			return new ResponseEntity<>("deleted", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ResponseBody
	@RequestMapping("/displayFile")
	public ResponseEntity<byte[]> displayFile(String fileName) throws Exception {
		logger.info("display File .....fileName={}", fileName);
		
		InputStream in = null;
		try {
			String formatName = FileUtils.getFileExtension(fileName);
			MediaType mType = FileUtils.getMediaType(formatName);
			HttpHeaders headers = new HttpHeaders();
			
			File file = new File(uploadPath + fileName);
			logger.info("exists={}", file.exists());
			if (!file.exists())
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			
			in = new FileInputStream(file);
			
			if (mType != null) {
				headers.setContentType(mType);
				
			} else {
				fileName = fileName.substring(fileName.indexOf("_") + 1);
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				String dsp = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
				logger.info("dsp={}", dsp);
				headers.add("Content-Disposition", 
						"attachment; filename=\"" + dsp + "\"" );
			}
			
			return new ResponseEntity<>(IOUtils.toByteArray(in), headers, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	@RequestMapping(value = "/uploadForm", method = RequestMethod.POST)
	public void uploadFormPOST(MultipartFile file, Model model, @RequestParam String type) throws Exception {
		logger.info("upload POST .....orinalName={}, size={}, contentType={}", 
				file.getOriginalFilename(),
				file.getSize(),
				file.getContentType());
		logger.info("......type={}", type);
		
		String savedFileName = FileUtils.uploadFile(file, uploadPath);
		model.addAttribute("savedFileName", savedFileName);
		model.addAttribute("type", type);
	}

}
