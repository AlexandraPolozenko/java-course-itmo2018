package ru.ifmo.rain.polozenko.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return sortStudentsById(students).stream()
                .findFirst()
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return streamFind(students.stream(), s -> name.equals(s.getFirstName()));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return streamFind(students.stream(), s -> name.equals(s.getLastName()));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return streamFind(students.stream(), s -> group.equals(s.getGroup()));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream().filter(s -> group.equals(s.getGroup())).collect(Collectors
                .toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return streamGet(students, Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return collectionGet(students, Student::getGroup);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return collectionGet(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return collectionGet(students, Student::getLastName);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return collectionGet(students, s -> String.format("%s %s", s.getFirstName(), s.getLastName()));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return streamSort(students.stream(), comp);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return streamSort(students.stream(), Comparator.comparing(Student::getId));
    }

    private Stream<String> streamGet(List<Student> students, Function<Student, String> fun) {
        return students.stream().map(fun);
    }

    private List<Student> streamSort(Stream<Student> students, Comparator<Student> comparator) {
        return students.sorted(comparator).collect(Collectors.toList());
    }

    private List<Student> streamFind(Stream<Student> students, Predicate<Student> predicate) {
        return streamSort(students.filter(predicate), comp);
    }

    private List<String> collectionGet(List<Student> students, Function<Student, String> fun) {
        return streamGet(students, fun).collect(Collectors.toList());
    }

    private Comparator<Student> comp = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);
}