package com.example.pdftester.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DownloadController {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping(path="/download")
	public ResponseEntity<Resource> test() throws InterruptedException, IOException {
		// Download chromedriver from https://chromedriver.chromium.org
		// make sure chromedriver's version matches your local Chrome/Chromium version
		String chromeDriverPath = "/Path/To/chromedriver";
		System.setProperty("webdriver.chrome.driver",chromeDriverPath);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--test-type", "--disable-extensions", "--disable-gpu", "--ignore-certificate-errors");
		// options.setBinary("/Path/to/specific/version/of/Google Chrome");

		ChromeDriverService driverService = ChromeDriverService.createDefaultService();
		ChromeDriver driver = new ChromeDriver(driverService, options);

		Map<String, Object> commandParams = new HashMap<>();
		commandParams.put("cmd", "Page.setDownloadBehavior");
		Map<String, String> params = new HashMap<>();
		params.put("behavior", "allow");
		params.put("downloadPath", "/Users/colin/Documents/pdftester/pdfs");
		commandParams.put("params", params);

		String url = driverService.getUrl().toString() + "/session/" + driver.getSessionId() + "/chromium/send_command";
		HttpPost request = new HttpPost(url);
		request.addHeader("content-type", "application/json");
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(commandParams)));
		HttpClientBuilder.create().build().execute(request);

		File file = new File("/Users/colin/Documents/pdftester/pdfs/test.pdf");
		file.delete();

		driver.get("http://localhost:9999/index.html");
		WebElement element = driver.findElement(By.id("download"));
		element.click();

		while (!file.exists()) {
			Thread.sleep(1000);
		}
	//	file.renameTo(new File("/Users/colin/Documents/pdftester/pdfs/test.pdf"));
		driver.quit();

		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=test.pdf")
				.contentType(MediaType.parseMediaType("application/pdf"))
				.body(resource);
	}
}
