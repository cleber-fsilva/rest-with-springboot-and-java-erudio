package br.com.erudio.services;

import br.com.erudio.controllers.PersonController;
import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.data.dto.v2.PersonDTOv2;
import br.com.erudio.exception.BadRequestException;
import br.com.erudio.exception.FileStorageException;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import static br.com.erudio.mapper.ObjectMapper.parseObjects;

import br.com.erudio.file.exporter.MediaTypes;
import br.com.erudio.file.exporter.contract.FileExporter;
import br.com.erudio.file.exporter.factory.FileExporterFactory;
import br.com.erudio.file.importer.contract.FileImporter;
import br.com.erudio.file.importer.factory.FileImporterFactory;
import br.com.erudio.mapper.custom.PersonMapper;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class PersonServices {

    private Logger logger = LoggerFactory.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    FileImporterFactory importer;

    @Autowired
    FileExporterFactory exporter;

    @Autowired
    PersonMapper converter;

    @Autowired
    PagedResourcesAssembler<PersonDTO> assembler;

    public PagedModel<EntityModel<PersonDTO>> findAll(Pageable pegeable) {
        logger.info("Finding all People!");

        var people = repository.findAll(pegeable);

        return buildPageModel(pegeable, people);
    }

    public PagedModel<EntityModel<PersonDTO>> findByName(String fistName, Pageable pegeable) {
        logger.info("Finding People by name!");

        var people = repository.findPeopleByName(fistName, pegeable);

        return buildPageModel(pegeable, people);
    }


    public PersonDTO findById(Long id) {
        logger.info("Finding one Person!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var dto =  parseObjects(entity, PersonDTO.class);
        addHateosLinks(dto);
        return dto;
    };

    public Resource exportPage(Pageable pegeable, String acceptHeader) {
        logger.info("Exporting a People page!");

        var people = repository.findAll(pegeable).map(person -> parseObjects(person, PersonDTO.class)).getContent();

        try {
            FileExporter exporter = this.exporter.getExporter(acceptHeader);
            return exporter.exportFile(people);
        } catch (Exception e) {
            throw new RuntimeException("Error during file export!", e);
        }
    }

    public PersonDTO create(PersonDTO person) {

        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Person!");
        var entity = parseObjects(person, Person.class);

        var dto = parseObjects(repository.save(entity), PersonDTO.class);
        addHateosLinks(dto);
        return dto;
    };

    public List<PersonDTO> massCreation(MultipartFile file) {
        logger.info("Importing People from File!");

        if (file.isEmpty()) throw new BadRequestException("Please set a Valid File!");

        try(InputStream inputStream = file.getInputStream()) {
            String fileName = Optional.ofNullable(file.getOriginalFilename())
                    .orElseThrow(() -> new BadRequestException("File Name cannot be null"));

            FileImporter importer = this.importer.getImporter(fileName);

            List<Person> entityes = importer.importFile(inputStream)
                    .stream()
                    .map(dto -> repository.save(parseObjects(dto, Person.class)))
                    .toList();

            return entityes.stream()
                    .map(entity -> {
                    var dto = parseObjects(entity, PersonDTO.class);
                    addHateosLinks(dto);
                    return dto;})
                    .toList();
        } catch (Exception e) {
            throw new FileStorageException("Error Processing the File!");
        }
    }

    public PersonDTOv2 createv2(PersonDTOv2 person) {

        logger.info("Creating one Person V2!");
        var entity = converter.covertDTOtoEntity(person);

        return converter.covertEntityToDTO(repository.save(entity));
    };

    @Transactional
    public PersonDTO disabledPerson(Long id) {
        logger.info("Disabling one person!");

        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.disablePerson(id);

        var entity = repository.findById(id).get();

        var dto = parseObjects(entity, PersonDTO.class);
        addHateosLinks(dto);
        return dto;

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

    private PagedModel<EntityModel<PersonDTO>> buildPageModel(Pageable pegeable, Page<Person> people) {

        var peopleWithLinks = people.map(person -> {
            var dto = parseObjects(person, PersonDTO.class);
            addHateosLinks(dto);
            return dto;
        });

        Link findAllLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PersonController.class)
                .findAll(pegeable.getPageNumber(), pegeable.getPageSize(), String.valueOf(pegeable.getSort()))).withSelfRel();

        return assembler.toModel(peopleWithLinks, findAllLink);
    }

    private void addHateosLinks(PersonDTO dto) {
        dto.add(linkTo(methodOn(PersonController.class).findById(dto.getId())).withSelfRel().withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).findAll(1, 12, "asc")).withRel("findAll").withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).findByName("" ,1, 12, "asc")).withRel("findByName").withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).create(dto)).withRel("create").withType("POST"));

        dto.add(linkTo(methodOn(PersonController.class)).slash("massCreation").withRel("massCreation").withType("POST"));

        dto.add(linkTo(methodOn(PersonController.class).update(dto)).withRel("update").withType("PUT"));

        dto.add(linkTo(methodOn(PersonController.class).disabledPerson(dto.getId())).withRel("disabled").withType("PATCH"));

        dto.add(linkTo(methodOn(PersonController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));

        dto.add(linkTo(methodOn(PersonController.class).exportPage(1, 12, "asc", null)).withRel("exportPage").withType("GET").withTitle("export People"));
    };
}
