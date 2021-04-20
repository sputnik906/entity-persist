package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class Employee {

  @NonNull
  private String id;

  @NonNull
  private String label;

  @NonNull
  private Set<Skill> skills;

  private List<String> phones = new ArrayList<>();

  @Setter  @NonNull
  private Address address;

  private Map<String,String> attributes = new HashMap<>();

  @NonNull
  private int age;

  @Setter
  private Position position;
}
