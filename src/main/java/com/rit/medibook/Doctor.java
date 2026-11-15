package com.rit.medibook;

import java.util.List;

public record Doctor(long id, String name, String speciality, List<String> slots) {
}
