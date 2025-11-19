package org.turntabl.chatapp.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Role {
    UUID id;
    String name;
}
