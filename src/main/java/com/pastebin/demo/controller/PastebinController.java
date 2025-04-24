package com.pastebin.demo.controller;

import com.pastebin.demo.dto.PastebinDto;
import com.pastebin.demo.entity.PastebinEntity;
import com.pastebin.demo.entity.UserEntity;
import com.pastebin.demo.repository.PastebinRepository;
import com.pastebin.demo.repository.UserRepository;
import com.pastebin.demo.service.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pastebin")
public class PastebinController {

    @Autowired
    PastebinRepository pastebinRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RandomGenerator randomGenerator;

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok(username);
    }
    @GetMapping("/all")
    public ResponseEntity<List<PastebinDto>> getAllPastebin() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username  = auth.getName();

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(username);
        UserEntity userEntity = optionalUserEntity.get();

        List<PastebinDto> pastebinDtoList = new ArrayList<>();

        List<PastebinEntity> pastebinEntities = userEntity.getPastebinEntities();
        for (PastebinEntity pastebinEntity : pastebinEntities) {
            PastebinDto pastebinDto = new PastebinDto();
            pastebinDto.setName(pastebinEntity.getName());
            pastebinDto.setDescription(pastebinEntity.getDescription());
            pastebinDto.setContent(pastebinEntity.getContent());
            pastebinDto.setExpireDate(pastebinEntity.getExpireDate());
            pastebinDtoList.add(pastebinDto);
        }

        return new ResponseEntity<>(pastebinDtoList, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('GET_HASH')")
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

    @PreAuthorize("hasRole('HASH')")
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody PastebinDto pastebinDto) {

        if (Timestamp.valueOf(LocalDateTime.now()).after(pastebinDto.getExpireDate())) {
            return new ResponseEntity<>("Tarih geçersiz", HttpStatus.BAD_REQUEST);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity  = (UserEntity) auth.getPrincipal();

        String hash = randomGenerator.generate(10);

        PastebinEntity pastebinEntity = new PastebinEntity();
        pastebinEntity.setName(pastebinDto.getName());
        pastebinEntity.setDescription(pastebinDto.getDescription());
        pastebinEntity.setContent(pastebinDto.getContent());
        pastebinEntity.setExpireDate(pastebinDto.getExpireDate());
        pastebinEntity.setHash(hash);
        pastebinEntity.setUserEntity(userEntity);
        pastebinRepository.save(pastebinEntity);

        return new ResponseEntity<>(hash, HttpStatus.OK);

    }


}
