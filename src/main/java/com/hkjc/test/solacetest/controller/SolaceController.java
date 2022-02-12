package com.hkjc.test.solacetest.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.hkjc.test.solacetest.dto.TransactionDto;
import com.hkjc.test.solacetest.service.TransactionService;

@RestController
public class SolaceController {

	private final TransactionService transService;
	@Autowired
	private JmsTemplate jmsTemplate;

    public SolaceController(TransactionService transService) {
        this.transService = transService;
    }
    
	@GetMapping("/api/getcontent")
	public TransactionDto getTransaction(@RequestParam String Id) throws NotFoundException{
		TransactionDto dto = transService.findTransactionById(Id);
	    return dto;
	}
	
	@GetMapping("/api/getlastmessageid")
	public String getLastMessageId(){
	    String Id = transService.findLastMessageId();
	    if (Id == null) {
	    	return "no message";
	    }
	    else 
	    {
	    	return Id;
	    }
	}
	
	@GetMapping("/api/getallmessages")
	public List<TransactionDto> getAllMessages(){
	    return transService.findAllMessages();
	}
	
	@PostMapping("/api/saveTest")
	public ResponseEntity<Void> saveContent(@RequestBody String json, UriComponentsBuilder uriComponentsBuilder){
        String transId = transService.saveMessage(json, "I", "");
        UriComponents uriComponents = uriComponentsBuilder.path("/api/getcontent?Id={id}").buildAndExpand(transId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity(transId, headers,HttpStatus.CREATED);
    }
	
	@PostConstruct
	private void customizeJmsTemplate() {
		// Update the jmsTemplate's connection factory to cache the connection
		CachingConnectionFactory ccf = new CachingConnectionFactory();
		ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
		jmsTemplate.setConnectionFactory(ccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination
		jmsTemplate.setPubSubDomain(false);
	}
	
	public void sendEvent(String msgContent) throws Exception {
		System.out.println("==========SENDING MESSAGE========== " + msgContent);
		jmsTemplate.convertAndSend("spring/test/topic/in", msgContent);
	}
	
	@PostMapping("api/sendSolaceMessage")
	public ResponseEntity<Void> sendSolaceMessage(@RequestBody String messageContent, UriComponentsBuilder uriComponentsBuilder) throws Exception{
		transService.pendingQueue.add(messageContent);
		String transId = null;
		Integer retry = 0;
		while (transId == null || retry > 10) {
			Thread.sleep(100);
			transId = transService.doneQueue.poll();
			if (transId == null) {
				retry++;
			}
		}
        return new ResponseEntity(transId, HttpStatus.CREATED);
	}
}
