package com.kasina.makeitshort.service;

import com.google.common.hash.Hashing;
import com.kasina.makeitshort.model.MakeItShort;
import com.kasina.makeitshort.model.MakeItShortDto;
import com.kasina.makeitshort.repository.MakeItShortRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class MakeItShortServiceImpl implements MakeItShortService {

    private final MakeItShortRepo makeItShortRepository;

    @Override
    public MakeItShort generateShortLink(MakeItShortDto makeItShortDto) {
        // TODO: check if url already exist in the database then return it rather than saving the same url multiple time

        if (StringUtils.isNotEmpty(makeItShortDto.getUrl())){
            String encodedUrl = encodeUrl(makeItShortDto.getUrl());

            MakeItShort urlToPersist = new MakeItShort();
            System.out.println(" Current Url ID is: ******" +urlToPersist.getUrlId());
            urlToPersist.setCreationDate(LocalDateTime.now());
            urlToPersist.setOriginalUrl(makeItShortDto.getUrl());
            urlToPersist.setShortLink(encodedUrl);
            urlToPersist.setExpirationDate(getExpirationDate(makeItShortDto.getExpirationDate(),
                    urlToPersist.getCreationDate()));
            return persistShortLink(urlToPersist);
        }
        return null;
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        // TODO: check if date is in the correct format
        if(StringUtils.isBlank(expirationDate)){
            return creationDate.plusSeconds(120);
        }

        return LocalDateTime.parse(
                expirationDate + "T00:00:00",  // Append a default time of 00:00:00
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }

    private String encodeUrl(String url) {

        LocalDateTime time = LocalDateTime.now();
        return Hashing.murmur3_32()
                .hashString(url.concat(time.toString()), StandardCharsets.UTF_8).toString();
    }

    @Override
    public MakeItShort persistShortLink(MakeItShort makeItShort) {
        return makeItShortRepository.save(makeItShort);
    }

    @Override
    public MakeItShort getEncoderUrl(String url) {
        return makeItShortRepository.findByShortLink(url);
    }

    @Override
    public void deleteShortLink(String urlId) {
        if (StringUtils.isEmpty(urlId) && makeItShortRepository.findById(urlId).isEmpty()){

            throw new IllegalStateException("Url does not exist or it might have expired");
        }
        makeItShortRepository.deleteById(urlId);
    }

//    public void deleteShortLink(String urlId) {
//        makeItShortRepository.deleteById(urlId);
//    }
}
