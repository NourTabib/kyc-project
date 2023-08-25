package com.hydatis.KycmicroserviceCQRS.query.eventlistener.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hydatis.KycmicroserviceCQRS.events.CreateEvent;
import com.hydatis.KycmicroserviceCQRS.events.DeleteEvent;
import com.hydatis.KycmicroserviceCQRS.events.Event;
import com.hydatis.KycmicroserviceCQRS.events.UpdateEvent;
import com.hydatis.KycmicroserviceCQRS.query.document.AgentPersonnePhysique;
import com.hydatis.KycmicroserviceCQRS.query.eventlistener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

@Component
@EnableKafka
public class PersonnePhysiqueEventListener implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            PersonnePhysiqueEventListener.class
    );
    private final ObjectMapper objectMapper = new ObjectMapper();
    public PersonnePhysiqueEventListener(){
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapper.registerModule(new JavaTimeModule());
    }
    @KafkaListener(topics = "agent.personne.physique.events",groupId = "pp-query-service")
    @Override
    public void consume(String commandeEvent) {
        serializeAndExecute(commandeEvent);
    }
    public AgentPersonnePhysique serializeAndExecute(String event){
        try {
            Map<String,String> eventRecord = objectMapper.readValue(event,Map.class);
            String type = eventRecord.getOrDefault("type","null");
            String jpaEntityJson = null;
            //INVALID EVENT
            if(eventRecord.getOrDefault("data","null").equals("null") || type.equals("null")){
                return null;
            }

            // COMMAND EXECUTION BASED ON THE EVENT TYPE
            // 1- INSTANTIATE AN EVENT FROM THE STRING
            // 2- WRITING THE EVENT DATA (PP Entity) AS A STRING (IN ORDER TO MAP IT TO A DOCUMENT )
            // 3- CONVERTING THE PP_ENTITY JSON TO PP_DOCUMENT
            // 4- EXECUTE
            if(type.contains("CreateEvent")){
                CreateEvent<com.hydatis.KycmicroserviceCQRS.command.model.AgentPersonnePhysique> createEvent = objectMapper.readValue(event,CreateEvent.class);
                jpaEntityJson = objectMapper.writeValueAsString(createEvent.getData());
                AgentPersonnePhysique agentPersonnePhysique =  objectMapper.readValue(jpaEntityJson,AgentPersonnePhysique.class);
                //REPOSITORY INSERT
            }else if(type.contains("UpdateEvent")) {
                UpdateEvent<com.hydatis.KycmicroserviceCQRS.command.model.AgentPersonnePhysique> updateEvent = objectMapper.readValue(event,UpdateEvent.class);
                jpaEntityJson = objectMapper.writeValueAsString(updateEvent.getData());
                AgentPersonnePhysique agentPersonnePhysique = objectMapper.readValue(jpaEntityJson,AgentPersonnePhysique.class);
                System.out.println(agentPersonnePhysique.toString());
                //REPOSITORY UPDATE
            }else if(type.contains("DeleteEvent")){
                DeleteEvent<com.hydatis.KycmicroserviceCQRS.command.model.AgentPersonnePhysique> deleteEvent = objectMapper.readValue(event,DeleteEvent.class);
                jpaEntityJson = objectMapper.writeValueAsString(deleteEvent.getData());
                AgentPersonnePhysique agentPersonnePhysique = objectMapper.readValue(jpaEntityJson,AgentPersonnePhysique.class);
                //REPOSITORY DELETE
            }else{

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
