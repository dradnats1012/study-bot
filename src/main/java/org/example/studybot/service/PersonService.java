package org.example.studybot.service;

import org.example.studybot.dto.person.CreatePersonDTO;
import org.example.studybot.model.Person;
import org.example.studybot.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    public Person createPerson(CreatePersonDTO createPersonDTO){
        Person person = Person.builder()
            .discordId(createPersonDTO.discordId())
            .nickName(createPersonDTO.nickName())
            .build();

        personRepository.save(person);

        return person;
    }

    public void deletePerson(Long id){
        personRepository.deleteById(id);
    }
}
