package com.inn.cafe.rest;

import com.inn.cafe.POJO.Bill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/bill")
public interface BillRest {

    @PostMapping(path = "/generatedReport")
    ResponseEntity<String> generatedReport(@RequestBody Map<String, Object> requestMap);

    @RequestMapping(path = "/getBills")
    ResponseEntity<List<Bill>> getBills();

    @PostMapping(path = "/getpdf")
    ResponseEntity<byte[]> getpdf(@RequestBody Map<String, Object> requestMap);

    @PostMapping(path ="/delete/{id}")
    ResponseEntity<String> deleteBill(@PathVariable Integer id);
}
