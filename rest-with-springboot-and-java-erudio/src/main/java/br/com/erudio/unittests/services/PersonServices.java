package br.com.erudio.unittests.services;

import br.com.erudio.controllers.PersonController;
import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.data.dto.v2.PersonDTOv2;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import static br.com.erudio.mapper.ObjectMapper.parseListObjects;
import static br.com.erudio.mapper.ObjectMapper.parseObjects;

import br.com.erudio.mapper.custom.PersonMapper;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonServices {

    private Logger logger = LoggerFactory.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    PersonMapper converter;

    public List<PersonDTO> findAll() {
        logger.info("Finding all People!");

        var persons =  parseListObjects(repository.findAll(), PersonDTO.class);
        persons.forEach(this::addHateosLinks);
        return persons;
    }

       public PersonDTO findById(Long id) {
        logger.info("Finding one Person!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var dto =  parseObjects(entity, PersonDTO.class);
        addHateosLinks(dto);
        return dto;
    };

    public PersonDTO create(PersonDTO person) {

        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Person!");
        var entity = parseObjects(person, Person.class);

        var dto = parseObjects(repository.save(entity), PersonDTO.class);
        addHateosLinks(dto);
        return dto;
    };

    public PersonDTOv2 createv2(PersonDTOv2 person) {

        logger.info("Creating one Person V2!");
        var entity = converter.covertDTOtoEntity(person);

        return converter.covertEntityToDTO(repository.save(entity));
    };

    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.delete(entity);

    };


    public PersonDTO update(PersonDTO person) {

        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Person!");

        Person entity =  repository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var dto = parseObjects(repository.save(entity), PersonDTO.class);
        addHateosLinks(dto);
        return dto;
    }

    private void addHateosLinks(PersonDTO dto) {
        dto.add(linkTo(methodOn(PersonController.class).findById(dto.getId())).withSelfRel().withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).findAll()).withRel("findAll").withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).create(dto)).withRel("create").withType("POST"));

        dto.add(linkTo(methodOn(PersonController.class).update(dto)).withRel("update").withType("PUT"));

        dto.add(linkTo(methodOn(PersonController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));
    };
}
