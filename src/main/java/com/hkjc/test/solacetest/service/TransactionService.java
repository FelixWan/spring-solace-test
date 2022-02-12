package com.hkjc.test.solacetest.service;

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

import org.springframework.context.annotation.Bean;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import com.hkjc.test.solacetest.domain.Transaction;
import com.hkjc.test.solacetest.dto.TransactionDto;
import com.hkjc.test.solacetest.repository.TransactionRepository;

@Service
public class TransactionService {
	
	private TransactionRepository transRepository;
	public final Queue<String> pendingQueue = new LinkedList<String>();
	public final Queue<String> doneQueue = new LinkedList<String>();
	
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
    
	@Bean
	public Consumer<Message<String>> messageConsumer() {
		return payload -> {
			System.out.println("Message Received: "+payload.toString());
			TransactionDto dto = new TransactionDto();
			dto.setIO("I");
			MessageHeaders headers = payload.getHeaders(); 
			dto.setId(headers.get("id").toString().replace("-", ""));
			dto.setContent(payload.getPayload());
			dto.setTopic(headers.get("solace_destination").toString());
			dto.setCreatedDateTime(LocalDateTime.now());
			saveMessage(dto);
		};
	}
	
	@Bean
	public Supplier<Message<String>> messageSupplier(){
		return () -> {
			String messageContent = pendingQueue.poll();
			if (messageContent == null) {
				return null;
			}
			TransactionDto dto = new TransactionDto();
			UUID uuid = UUID.randomUUID();
			dto.setIO("O");
			dto.setId(uuid.toString().replace("-", ""));
			dto.setContent(messageContent);
			dto.setTopic("spring/test/topic/out");
			dto.setCreatedDateTime(LocalDateTime.now());
			doneQueue.add(saveMessage(dto));
			System.out.println("Message pending: "+dto.getContent());
			Message<String> message = MessageBuilder.withPayload(dto.getContent()).setHeader("contentType", "application/json").build();
			return message;
		};
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
