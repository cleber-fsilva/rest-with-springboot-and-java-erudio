package br.com.erudio.file.importer.contract;

import br.com.erudio.data.dto.v1.PersonDTO;

import java.io.InputStream;
import java.util.List;

public interface FileImporter {

    List<PersonDTO> importFile(InputStream inputStream) throws Exception;
}
