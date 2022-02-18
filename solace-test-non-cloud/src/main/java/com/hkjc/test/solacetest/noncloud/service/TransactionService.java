package com.hkjc.test.solacetest.noncloud.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AckUtils;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import com.hkjc.test.solacetest.noncloud.domain.Transaction;
import com.hkjc.test.solacetest.noncloud.dto.TransactionDto;
import com.hkjc.test.solacetest.noncloud.repository.TransactionRepository;
import com.solacesystems.jms.message.SolMessage;
import com.solacesystems.jms.message.SolTextMessage;

@Service
public class TransactionService {
	
    @Autowired
	private JmsTemplate jmsTemplate;	
    private TransactionRepository transRepository;
	
    public TransactionService(TransactionRepository transRepository) {
        this.transRepository=transRepository;
    }
    
    public TransactionDto findTransactionById(String Id) throws NotFoundException {
    	Optional<Transaction> trans = transRepository.findById(Id);
        Transaction transEntity = trans.orElseThrow(NotFoundException::new);
        return toTransactionDto(transEntity);
    }
    
    public String findLastMessageId() {
    	List<Transaction> transList = transRepository.findAll();
    	if (transList == null || transList.isEmpty()) {
    		return null;
    	}
    	String Id = transList.stream()
		                   .sorted(Comparator.comparing(Transaction::getCreatedDateTime).reversed())
		                   .findFirst().get().getId();
    	return Id;
    }
    
    public List<TransactionDto> findAllMessages(){
    	List<Transaction> transList = transRepository.findAll();
    	List<TransactionDto> dtoList = new ArrayList<TransactionDto>();
    	transList.stream().forEach(entity -> dtoList.add(toTransactionDto(entity)));
    	return dtoList;
    }
    
    public String saveMessage(String content, String _IO, String topic) {
    	Transaction trans = new Transaction();
    	final String uuid = UUID.randomUUID().toString().replace("-", "");
    	trans.setContent(content);
    	trans.setCreatedDateTime(LocalDateTime.now());
    	trans.setIO(_IO);
    	trans.setId(uuid);
    	trans.setTopic(topic);
    	transRepository.save(trans);
        return uuid;
    }
    
    public String saveMessage(TransactionDto dto) {
    	Transaction trans = toTransactionEntity(dto);
    	transRepository.save(trans);
        return trans.getId();
    }
	
	public String sendEvent(String messageContent) throws Exception {
		TransactionDto dto = new TransactionDto();
		dto.setIO("O");
		dto.setContent(messageContent);
		dto.setTopic("spring/test/topic/out2");
		dto.setCreatedDateTime(LocalDateTime.now());		
		System.out.println("Message pending: "+dto.getContent());
		jmsTemplate.setPubSubDomain(true);
		jmsTemplate.setMessageIdEnabled(true);
		jmsTemplate.send(dto.getTopic(), new MessageCreator() {
			@Override
			public javax.jms.Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(messageContent);
			}
		});
		dto.setId(UUID.randomUUID().toString().replace("-", ""));
		this.saveMessage(dto);
		return dto.getId();
	}
	
	@JmsListener(destination = "spring/test/queue/2")
	public void handle(SolTextMessage message) throws JMSException {		
		System.out.println("Message Received: "+message.toString());
		TransactionDto dto = new TransactionDto();
		dto.setIO("I");
		String messageId = message.getJMSMessageID();
		if (messageId == null) {
			messageId = UUID.randomUUID().toString().replace("-", "");
		}
		dto.setId(messageId);
		dto.setContent(message.getText());
		dto.setTopic(message.getJMSDestination().toString());
		dto.setCreatedDateTime(LocalDateTime.now());
		saveMessage(dto);
		message.acknowledge();
	}
    
    private TransactionDto toTransactionDto(Transaction trans){
    	TransactionDto dto = new TransactionDto();
        dto.setId(trans.getId());
        dto.setIO(trans.getIO());
        dto.setContent(trans.getContent());
        dto.setTopic(trans.getTopic());
        dto.setCreatedDateTime(trans.getCreatedDateTime());
        return dto;
    }

    private Transaction toTransactionEntity(TransactionDto dto){
    	Transaction trans = new Transaction();
    	trans.setId(dto.getId());
    	trans.setIO(dto.getIO());
        trans.setContent(dto.getContent());
        trans.setTopic(dto.getTopic());
        trans.setCreatedDateTime(dto.getCreatedDateTime());
        return trans;
    }
}
