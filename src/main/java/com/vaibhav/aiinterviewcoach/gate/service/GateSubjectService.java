package com.vaibhav.aiinterviewcoach.gate.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GateSubjectService {

    private static final Map<String, List<String>> GATE_TAXONOMY = new HashMap<>();

    static {
        GATE_TAXONOMY.put("C Programming", Arrays.asList("Pointers", "Arrays", "Functions", "Recursion", "Memory Management"));
        GATE_TAXONOMY.put("Data Structures", Arrays.asList("Stacks", "Queues", "Linked Lists", "Trees", "Graphs", "Hashing"));
        GATE_TAXONOMY.put("Algorithms", Arrays.asList("Sorting", "Searching", "Greedy", "Dynamic Programming", "Divide and Conquer"));
        GATE_TAXONOMY.put("DBMS", Arrays.asList("ER Model", "Relational Model", "SQL", "Normalization", "Transactions", "Concurrency"));
        GATE_TAXONOMY.put("Operating Systems", Arrays.asList("Processes", "Threads", "CPU Scheduling", "Deadlocks", "Memory Management"));
        GATE_TAXONOMY.put("Computer Networks", Arrays.asList("OSI Model", "TCP/IP", "Routing", "Application Layer", "Network Security"));
        GATE_TAXONOMY.put("Computer Organization & Architecture", Arrays.asList("Machine Instructions", "ALU", "Data Path", "Memory Hierarchy", "I/O Interface"));
        GATE_TAXONOMY.put("Digital Logic", Arrays.asList("Boolean Algebra", "Combinational Circuits", "Sequential Circuits", "Number Representations"));
        GATE_TAXONOMY.put("Discrete Mathematics", Arrays.asList("Propositional Logic", "Set Theory", "Combinatorics", "Graph Theory"));
        GATE_TAXONOMY.put("Theory of Computation", Arrays.asList("Regular Expressions", "Finite Automata", "Context-Free Grammars", "Turing Machines"));
        GATE_TAXONOMY.put("Compiler Design", Arrays.asList("Lexical Analysis", "Parsing", "Syntax Directed Translation", "Code Optimization"));
        GATE_TAXONOMY.put("Engineering Mathematics", Arrays.asList("Linear Algebra", "Calculus", "Probability"));
        GATE_TAXONOMY.put("General Aptitude", Arrays.asList("Verbal Ability", "Quantitative Aptitude", "Analytical Aptitude", "Spatial Aptitude"));
    }

    public List<String> getSubjects() {
        return GATE_TAXONOMY.keySet().stream().toList();
    }

    public List<String> getTopics(String subject) {
        return GATE_TAXONOMY.getOrDefault(subject, List.of());
    }

    public boolean isValidSubject(String subject) {
        return GATE_TAXONOMY.containsKey(subject);
    }
}
