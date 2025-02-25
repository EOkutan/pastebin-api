package com.pastebin.demo.controller;

import com.pastebin.demo.dto.PastebinDto;
import com.pastebin.demo.entity.PastebinEntity;
import com.pastebin.demo.repository.PastebinRepository;
import com.pastebin.demo.service.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/pastebin")
public class PastebinController {

    @Autowired
    PastebinRepository pastebinRepository;

    @Autowired
    RandomGenerator randomGenerator;

    @GetMapping("/{hash}")
    public ResponseEntity<Object> getContent(@PathVariable("hash") String hash) {
        PastebinEntity pastebinEntity = pastebinRepository.findByHash(hash);
        if (pastebinEntity == null) {
            return new ResponseEntity<>("Kayıt bulunamadı", HttpStatus.NOT_FOUND);
        }

        if (Timestamp.valueOf(LocalDateTime.now()).after(pastebinEntity.getExpireDate())) {
            return new ResponseEntity<>("İçeriğin süresi dolmuş", HttpStatus.BAD_REQUEST);
        }

        PastebinDto pastebinDto = new PastebinDto();
        pastebinDto.setName(pastebinEntity.getName());
        pastebinDto.setDescription(pastebinEntity.getDescription());
        pastebinDto.setContent(pastebinEntity.getContent());
        pastebinDto.setExpireDate(pastebinEntity.getExpireDate());

        return new ResponseEntity<>(pastebinDto, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody PastebinDto pastebinDto) {

        if (Timestamp.valueOf(LocalDateTime.now()).after(pastebinDto.getExpireDate())) {
            return new ResponseEntity<>("Tarih geçersiz", HttpStatus.BAD_REQUEST);
        }

        String hash = randomGenerator.generate(10);

        PastebinEntity pastebinEntity = new PastebinEntity();
        pastebinEntity.setName(pastebinDto.getName());
        pastebinEntity.setDescription(pastebinDto.getDescription());
        pastebinEntity.setContent(pastebinDto.getContent());
        pastebinEntity.setExpireDate(pastebinDto.getExpireDate());
        pastebinEntity.setHash(hash);
        pastebinRepository.save(pastebinEntity);

        return new ResponseEntity<>(hash, HttpStatus.OK);

    }
}
