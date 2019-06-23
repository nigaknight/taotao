package com.taotao.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface PictureService {
	public Map uploadPicture(MultipartFile uploadFile);
}
