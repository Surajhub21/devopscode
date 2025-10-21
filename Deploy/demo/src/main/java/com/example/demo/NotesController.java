package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    private  MongoDBRepo notes;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Notes> listNotes() {
        return notes.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Notes addNote(@RequestBody Notes note) {
        if (note.getId() == null || note.getId().isBlank()) {
            note.setId(UUID.randomUUID().toString());
        }
        notes.save(note);
        return note;
    }
}

