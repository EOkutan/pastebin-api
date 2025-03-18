package com.pastebin.demo.repository;

import com.pastebin.demo.entity.PastebinEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PastebinRepository extends CrudRepository<PastebinEntity, Long> {

    PastebinEntity findByHash(String hash);

    PastebinEntity findByDescription(String description);
}
