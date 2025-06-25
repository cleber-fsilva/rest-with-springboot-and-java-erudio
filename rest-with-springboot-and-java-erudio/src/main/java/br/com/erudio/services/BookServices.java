package br.com.erudio.services;

import br.com.erudio.controllers.BookController;
import br.com.erudio.controllers.BookController;
import br.com.erudio.data.dto.v1.BookDTO;
import br.com.erudio.data.dto.v1.BookDTO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.model.Book;
import br.com.erudio.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.erudio.mapper.ObjectMapper.parseListObjects;
import static br.com.erudio.mapper.ObjectMapper.parseObjects;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookServices {

    private Logger logger = LoggerFactory.getLogger(BookServices.class.getName());

    @Autowired
    BookRepository repository;

    @Autowired
    PagedResourcesAssembler<BookDTO> assembler;

    public PagedModel<EntityModel<BookDTO>> findAll(Pageable pegeable) {
        logger.info("Finding all Book!");

        var books = repository.findAll(pegeable);

        var booksWithLinks = books.map(book -> {
            var dto = parseObjects(book, BookDTO.class);
            addHateosLinks(dto);
            return dto;
        });

        Link findAllLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class)
                .findAll(pegeable.getPageNumber(), pegeable.getPageSize(), String.valueOf(pegeable.getSort()))).withSelfRel();

        return assembler.toModel(booksWithLinks, findAllLink);
    }

       public BookDTO findById(Long id) {
        logger.info("Finding one Book!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var dto =  parseObjects(entity, BookDTO.class);
        addHateosLinks(dto);
        return dto;
    };

    public BookDTO create(BookDTO book) {

        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Book!");
        var entity = parseObjects(book, Book.class);

        var dto = parseObjects(repository.save(entity), BookDTO.class);
        addHateosLinks(dto);
        return dto;
    };


    public void delete(Long id) {
        logger.info("Deleting one book!");

        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.delete(entity);

    };


    public BookDTO update(BookDTO book) {

        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Book!");

        Book entity =  repository.findById(book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setAuthor(book.getAuthor());
        entity.setLaunchDate(book.getLaunchDate());
        entity.setPrice(book.getPrice());
        entity.setTitle(book.getTitle());

        var dto = parseObjects(repository.save(entity), BookDTO.class);
        addHateosLinks(dto);
        return dto;
    }

    private void addHateosLinks(BookDTO dto) {
        dto.add(linkTo(methodOn(BookController.class).findById(dto.getId())).withSelfRel().withType("GET"));

        dto.add(linkTo(methodOn(BookController.class).findAll(1, 12, "asc")).withRel("findAll").withType("GET"));

        dto.add(linkTo(methodOn(BookController.class).create(dto)).withRel("create").withType("POST"));

        dto.add(linkTo(methodOn(BookController.class).update(dto)).withRel("update").withType("PUT"));

        dto.add(linkTo(methodOn(BookController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));
    };
}
