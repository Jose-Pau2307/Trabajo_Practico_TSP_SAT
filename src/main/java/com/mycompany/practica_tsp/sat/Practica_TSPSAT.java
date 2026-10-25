/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.practica_tsp.sat;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 *
 * @author Usuario
 */
public class Practica_TSPSAT {

    static final class Cnf {
        final List<int[]> clauses = new ArrayList<>();
        int vars = 0;
        int trueVar, falseVar;
        Cnf() {
            trueVar = newVar(); add(trueVar);
            falseVar = newVar(); add(-falseVar);
        }
        int newVar() { return ++vars; }
        void add(int... lits) { clauses.add(lits); }
        int and(int a, int b) {
            int z = newVar();
            add(-z, a); add(-z, b); add(z, -a, -b);
            return z;
        }
        int or(int a, int b) {
            int z = newVar();
            add(z, -a); add(z, -b); add(-z, a, b);
            return z;
        }
        int xor(int a, int b) {
            int z = newVar();
            add(-a, -b, -z); add(a, b, -z);
            add(a, -b, z); add(-a, b, z);
            return z;
        }
        int eq(int a, int b) {
            int z = newVar();
            add(-z, -a, b); add(-z, a, -b);
            add(z, a, b); add(z, -a, -b);
            return z;
        }
        int not(int a) {
            int z = newVar();
            add(-z, -a); add(z, a);
            return z;
        }
        int fullAdderSum(int a, int b, int carry) {
            return xor(xor(a, b), carry);
        }
        int fullAdderCarry(int a, int b, int carry) {
            int ab = and(a,b), ac = and(a,carry), bc = and(b,carry);
            return or(or(ab,ac),bc);
        }
        void exactlyOne(int[] xs) {
            add(xs);
            for (int i=0;i<xs.length;i++)
                for (int j=i+1;j<xs.length;j++) add(-xs[i],-xs[j]);
        }
        void atMostOne(int[] xs) {
            for (int i=0;i<xs.length;i++)
                for (int j=i+1;j<xs.length;j++) add(-xs[i],-xs[j]);
        }
        void writeDimacs(Path p) throws IOException {
            try (BufferedWriter w = Files.newBufferedWriter(p)) {
                w.write("p cnf " + vars + " " + clauses.size()); w.newLine();
                for (int[] c : clauses) {
                    for (int lit : c) { w.write(Integer.toString(lit)); w.write(' '); }
                    w.write("0"); w.newLine();
                }
            }
        }
    }

    static int[][] d;
    static int n, k;
    static int[][] x;
    static Cnf cnf;

    static int[] addConstantTimesLiteral(int[] sum, int literal, int weight) {
        int[] result = new int[sum.length];
        int carry = cnf.falseVar;
        for (int b=0;b<sum.length;b++) {
            int operand = ((weight >>> b) & 1) == 1 ? literal : cnf.falseVar;
            result[b] = cnf.fullAdderSum(sum[b], operand, carry);
            carry = cnf.fullAdderCarry(sum[b], operand, carry);
        }
        // La anchura se elige para que el desbordamiento no pueda ocurrir.
        cnf.add(-carry);
        return result;
    }

    static int lessOrEqual(int[] a, int limit) {
        int equal = cnf.trueVar;
        int less = cnf.falseVar;
        for (int b=a.length-1;b>=0;b--) {
            int kb = (limit >>> b) & 1;
            if (kb == 1) {
                int notA = cnf.not(a[b]);
                less = cnf.or(less, cnf.and(equal, notA));
                equal = cnf.and(equal, a[b]);
            } else {
                equal = cnf.and(equal, cnf.not(a[b]));
            }
        }
        return cnf.or(less, equal);
    }

    static void encode() {
        cnf = new Cnf();
        x = new int[n][n];
        for (int i=0;i<n;i++) for (int t=0;t<n;t++) x[i][t]=cnf.newVar();

        // Cada ciudad aparece exactamente una vez.
        for (int i=0;i<n;i++) {
            int[] row = new int[n];
            for (int t=0;t<n;t++) row[t]=x[i][t];
            cnf.exactlyOne(row);
        }
        // En cada posición se ocupa exactamente una ciudad.
        for (int t=0;t<n;t++) {
            int[] col = new int[n];
            for (int i=0;i<n;i++) col[i]=x[i][t];
            cnf.exactlyOne(col);
        }

        // z[i][j][t] representa la transición i -> j entre t y t+1.
        int[][][] z = new int[n][n][n];
        List<Integer> terms = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (int t=0;t<n;t++) {
            int next=(t+1)%n;
            for (int i=0;i<n;i++) for (int j=0;j<n;j++) {
                if (i==j || d[i][j] < 0) {
                    // Prohíbe directamente seleccionar una transición inválida.
                    int zz=cnf.newVar(); z[i][j][t]=zz;
                    cnf.add(-zz);
                    cnf.add(-x[i][t], -x[j][next]);
                    continue;
                }
                int zz=cnf.and(x[i][t],x[j][next]);
                z[i][j][t]=zz;
                terms.add(zz); weights.add(d[i][j]);
            }
        }

        long maxTotal=0;
        for (int t=0;t<n;t++) {
            int maxEdge=0;
            for (int i=0;i<n;i++) for (int j=0;j<n;j++)
                if (i!=j && d[i][j]>=0) maxEdge=Math.max(maxEdge,d[i][j]);
            maxTotal += maxEdge;
        }
        int width=1;
        while (width<31 && (1L<<width) <= maxTotal) width++;
        width=Math.max(width,1);
        int[] sum = new int[width];
        Arrays.fill(sum, cnf.falseVar);
        for (int q=0;q<terms.size();q++) {
            int w=weights.get(q);
            if (w==0) continue;
            sum=addConstantTimesLiteral(sum,terms.get(q),w);
        }
        if (k < 0) cnf.add(cnf.falseVar); // unidad falsa: UNSAT para umbral negativo.
        else if (k < (1L<<Math.min(width,30))) cnf.add(lessOrEqual(sum,k));
        else {
            // K supera el máximo representable por la anchura usada: la cota es redundante.
            if (k < maxTotal) cnf.add(lessOrEqual(sum,k));
        }
    }

    static int[][] readInstance(Path file) throws IOException {
        List<String> tokens = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            String clean=line.replaceAll("#.*$","").trim();
            if (!clean.isEmpty()) tokens.addAll(Arrays.asList(clean.split("\\s+")));
        }
        if (tokens.size()<2) throw new IllegalArgumentException("Faltan N y K.");
        n=Integer.parseInt(tokens.get(0)); k=Integer.parseInt(tokens.get(1));
        if (n<2) throw new IllegalArgumentException("N debe ser al menos 2.");
        if (tokens.size()!=2+n*n) throw new IllegalArgumentException("Se esperan exactamente N*N distancias.");
        d=new int[n][n]; int p=2;
        for(int i=0;i<n;i++) for(int j=0;j<n;j++) {
            d[i][j]=Integer.parseInt(tokens.get(p++));
            if (i==j) d[i][j]=-1;
            else if (d[i][j] < -1) throw new IllegalArgumentException("Distancia inválida: use -1 para arista ausente.");
        }
        long maxEdge=0;
        for (int i=0;i<n;i++) for (int j=0;j<n;j++)
            if (i!=j && d[i][j]>=0) maxEdge=Math.max(maxEdge,d[i][j]);
        if (maxEdge*n > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Por simplicidad de esta versión, N*distancia máxima debe ser <= 2^31-1.");
        return d;
    }

    public static void main(String[] args) throws Exception {
        if (args.length<1) {
            System.err.println("Uso: java ... TspSat <instancia.txt> [salida.cnf]");
            System.exit(2);
        }
        Path input=Paths.get(args[0]);
        readInstance(input);
        encode();
        Path cnfPath=Paths.get(args.length>1 ? args[1] : "tsp_reduccion.cnf");
        cnf.writeDimacs(cnfPath);
        System.out.printf("Instancia: N=%d, K=%d%n",n,k);
        System.out.printf("CNF generada: %d variables, %d clausulas (%s)%n",
                cnf.vars,cnf.clauses.size(),cnfPath.toAbsolutePath());

        ISolver solver=SolverFactory.newDefault();
        solver.newVar(cnf.vars);
        solver.setExpectedNumberOfClauses(cnf.clauses.size());
        try {
            for (int[] clause:cnf.clauses) solver.addClause(new VecInt(clause));
            if (!solver.isSatisfiable()) {
                System.out.println("NO existe ninguna ruta con costo menor o igual a K");
                return;
            }
            boolean[] model=new boolean[cnf.vars+1];
            for (int lit:solver.model()) if (lit>0) model[lit]=true;
            int[] route=new int[n];
            Arrays.fill(route,-1);
            for(int i=0;i<n;i++) for(int t=0;t<n;t++) if(model[x[i][t]]) route[t]=i;
            long cost=0;
            System.out.print("Ruta encontrada: ");
            for(int t=0;t<n;t++) {
                if(route[t]<0) throw new IllegalStateException("Modelo SAT no reconstruible.");
                System.out.print(route[t] + " -> ");
                int next=route[(t+1)%n];
                if(d[route[t]][next]<0) throw new IllegalStateException("Transición inválida en el modelo.");
                cost+=d[route[t]][next];
            }
            System.out.println(route[0]);
            System.out.println("Costo real: "+cost);
            System.out.println("Verificación: "+(cost<=k ? "cumple costo <= K" : "ERROR: excede K"));
        } catch (ContradictionException e) {
            System.out.println("NO existe ninguna ruta con costo menor o igual a K");
        } catch (TimeoutException e) {
            System.out.println("El SAT-Solver alcanzó el límite de tiempo; resultado no concluyente.");
        }
    }
}
