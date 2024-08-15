package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.service.RetirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {ACCEPT_VERSION + "=" + API_V1})
@AllArgsConstructor
@Tag(name = "Retirement", description = "APIs related to retirement details")
public class RetirementController {

    public static final String ENDPOINT = "/retirement";

    private final RetirementService retirementService;

    @PostMapping(value = ENDPOINT, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create a new retirement detail")
    public ResponseEntity<RetirementDetailDTO> createRetirementDetail(@RequestBody RetirementDetailDTO retirementDetail) {
        RetirementDetailDTO retirementDetail1 = retirementService.createRetirementDetail(retirementDetail);
        return ResponseEntity.created(URI.create(ENDPOINT.concat("/").concat(String.valueOf(retirementDetail.id()))))
                .body(retirementDetail1);
    }

    @GetMapping(value = ENDPOINT + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get a retirement detail by ID")
    public ResponseEntity<RetirementDetailDTO> getRetirementDetail(@PathVariable Long id) {
        RetirementDetailDTO retirementDetail = retirementService.findById(id);
        return ResponseEntity.ok(retirementDetail);
    }

    @PutMapping(value = ENDPOINT + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Update a retirement detail by ID")
    public ResponseEntity<RetirementDetailDTO> updateRetirementDetail(@PathVariable Long id, @RequestBody RetirementDetailDTO retirementDetail) {
        RetirementDetailDTO updatedDetail = retirementService.updateRetirementDetail(id, retirementDetail);
        return ResponseEntity.ok(updatedDetail);
    }

    @DeleteMapping(value = ENDPOINT + "/{id}")
    @Operation(summary = "Delete a retirement detail by ID")
    public ResponseEntity<Void> deleteRetirementDetail(@PathVariable Long id) {
        retirementService.deleteRetirementDetail(id);
        return ResponseEntity.noContent().build();
    }

}
