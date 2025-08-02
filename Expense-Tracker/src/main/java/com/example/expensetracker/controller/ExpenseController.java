package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.services.ExpenseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // Home page
    @GetMapping("/")
    public String viewHomePage(Model model, HttpSession session) {
        List<Expense> expenses = expenseService.getAllExpenses();

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal userLimit = (BigDecimal) session.getAttribute("userLimit");
        boolean limitReached = userLimit != null && totalAmount.compareTo(userLimit) > 0;

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("userLimit", userLimit);
        model.addAttribute("limitReached", limitReached);

        return "index";
    }

    // Set user-defined expense limit
    @PostMapping("/setLimit")
    public String setLimit(@RequestParam("limit") BigDecimal limit, HttpSession session) {
        session.setAttribute("userLimit", limit);
        return "redirect:/";
    }

    // Show Add Expense Form
    @GetMapping("/addExpense")
    public String showAddExpensePage(Model model) {
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    // Save New Expense
    @PostMapping("/saveExpense")
    public String saveExpense(@ModelAttribute("expense") Expense expense) {
        expenseService.saveExpense(expense);
        return "redirect:/";
    }

    // Edit existing Expense
    @GetMapping("/editExpense/{id}")
    public String showUpdateExpensePage(@PathVariable("id") long id, Model model) {
        Expense expense = expenseService.getExpenseById(id);
        model.addAttribute("expense", expense);
        return "update-expense";
    }

    // Update Expense
    @PostMapping("/updateExpense/{id}")
    public String updateExpense(@PathVariable("id") long id, @ModelAttribute("expense") Expense updatedExpense) {
        Expense existingExpense = expenseService.getExpenseById(id);

        existingExpense.setDescription(updatedExpense.getDescription());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setDate(updatedExpense.getDate());
        existingExpense.setCategory(updatedExpense.getCategory());
        existingExpense.setPaymentMode(updatedExpense.getPaymentMode());

        expenseService.saveExpense(existingExpense);
        return "redirect:/";
    }

    // Delete Expense
    @GetMapping("/deleteExpense/{id}")
    public String deleteExpense(@PathVariable("id") long id) {
        expenseService.deleteExpenseById(id);
        return "redirect:/";
    }

    // View charts/visualization
    @GetMapping("/visualize")
    public String visualizeExpenses(Model model) {
        List<Expense> expenses = expenseService.getAllExpenses();
        model.addAttribute("expenses", expenses);
        return "visualize"; // loads visualize.html
    }
}
