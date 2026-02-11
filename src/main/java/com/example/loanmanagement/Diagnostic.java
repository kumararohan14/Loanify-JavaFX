package com.example.loanmanagement;

import com.example.loanmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Diagnostic {
    public static void main(String[] args) {
        try (PrintWriter out = new PrintWriter(new FileWriter("diagnostic_output.txt"))) {
            out.println("Starting Diagnostic...");
            try {
                out.println("Attempting to build SessionFactory...");
                SessionFactory sf = HibernateUtil.getSessionFactory();
                if (sf == null) {
                    out.println("SessionFactory is NULL. Initialization failed.");
                } else {
                    out.println("SessionFactory built successfully.");
                    try (Session session = sf.openSession()) {
                        out.println("Session opened successfully.");
                        out.println("Check: " + session.createNativeQuery("SELECT 1").getSingleResult());
                    }
                }
            } catch (Throwable e) {
                out.println("Exception occurred:");
                e.printStackTrace(out);
            } finally {
                HibernateUtil.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
