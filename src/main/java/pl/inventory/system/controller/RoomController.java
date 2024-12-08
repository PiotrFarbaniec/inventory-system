package pl.inventory.system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/rooms"}, produces = {"application/json;charset=UTF-8"})
public class RoomController {
}
