package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CreateTurfRequest;
import com.criczone.demo.dto.ApiRequests.NearbyTurfsRequest;
import com.criczone.demo.dto.ApiRequests.UpdateTurfRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.TurfService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turfs")
@Tag(name = "Turfs", description = "Turf inventory, nearby lookup, ownership, updates, and deletes")
public class TurfController {

    private final TurfService turfService;

    public TurfController(TurfService turfService) {
        this.turfService = turfService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                   @Valid @RequestBody CreateTurfRequest request) {
        return turfService.add(currentUser, RequestMaps.toMap(request));
    }

    @GetMapping("/all")
    public Map<String, Object> all() {
        return turfService.all();
    }

    @GetMapping("/owned")
    public Map<String, Object> owned(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return turfService.owned(currentUser);
    }

    @PostMapping("/nearby")
    public Map<String, Object> nearby(@Valid @RequestBody NearbyTurfsRequest request) {
        return turfService.nearby(RequestMaps.toMap(request));
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(@PathVariable String id) {
        return turfService.one(id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id,
                                      @Valid @RequestBody UpdateTurfRequest request) {
        return turfService.update(currentUser, id, RequestMaps.toMap(request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id) {
        return turfService.delete(currentUser, id);
    }
}
