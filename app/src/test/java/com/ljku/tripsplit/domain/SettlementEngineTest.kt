package com.ljku.tripsplit.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SettlementEngineTest {

    @Test
    fun testSimpleSplit() {
        // A pays 300, split among A, B, C equally
        val m1 = Member("1", "trip", "A")
        val m2 = Member("2", "trip", "B")
        val m3 = Member("3", "trip", "C")
        
        val members = listOf(m1, m2, m3)
        val expense = Expense(
            id = "e1",
            tripId = "trip",
            description = "Dinner",
            totalAmount = 300,
            date = 0,
            paidBy = listOf(PaidShare("1", 300)),
            splitAmong = listOf(
                SplitShare("1", 100),
                SplitShare("2", 100),
                SplitShare("3", 100)
            )
        )

        val balances = SettlementEngine.calculateBalances(members, listOf(expense))
        assertEquals(200L, balances["1"])
        assertEquals(-100L, balances["2"])
        assertEquals(-100L, balances["3"])

        val txs = SettlementEngine.simplifyDebts(balances)
        assertEquals(2, txs.size)
        // B pays A 100
        // C pays A 100
        val fromB = txs.find { it.fromMemberId == "2" }
        assertEquals("1", fromB?.toMemberId)
        assertEquals(100L, fromB?.amount)

        val fromC = txs.find { it.fromMemberId == "3" }
        assertEquals("1", fromC?.toMemberId)
        assertEquals(100L, fromC?.amount)
    }

    @Test
    fun testComplexSplitWithCircularDebt() {
        // A pays B 100, B pays C 100, C pays A 100 -> simplified to 0 transactions
        val m1 = Member("A", "trip", "A")
        val m2 = Member("B", "trip", "B")
        val m3 = Member("C", "trip", "C")

        val members = listOf(m1, m2, m3)
        val e1 = Expense("e1", "trip", "", 100, 0, listOf(PaidShare("A", 100)), listOf(SplitShare("B", 100)))
        val e2 = Expense("e2", "trip", "", 100, 0, listOf(PaidShare("B", 100)), listOf(SplitShare("C", 100)))
        val e3 = Expense("e3", "trip", "", 100, 0, listOf(PaidShare("C", 100)), listOf(SplitShare("A", 100)))

        val balances = SettlementEngine.calculateBalances(members, listOf(e1, e2, e3))
        assertEquals(0L, balances["A"])
        assertEquals(0L, balances["B"])
        assertEquals(0L, balances["C"])

        val txs = SettlementEngine.simplifyDebts(balances)
        assertEquals(0, txs.size)
    }

    @Test
    fun testEqualSplitsWithSubsetAndRemainder() {
        // 1000 paise (10 rupees) split among 3 members in whole rupees: 4 + 3 + 3 = 10
        val splits = SettlementEngine.buildEqualSplits(1000L, listOf("m1", "m2", "m3"))
        assertEquals(3, splits.size)
        assertEquals(1000L, splits.sumOf { it.amountOwed })
        assertEquals(400L, splits[0].amountOwed)
        assertEquals(300L, splits[1].amountOwed)
        assertEquals(300L, splits[2].amountOwed)

        // 50 rupees (5000 paise) split among 3 members: 17 + 17 + 16 = 50 (never 51!)
        val fiftySplits = SettlementEngine.buildEqualSplits(5000L, listOf("m1", "m2", "m3"))
        assertEquals(3, fiftySplits.size)
        assertEquals(5000L, fiftySplits.sumOf { it.amountOwed })
        assertEquals(1700L, fiftySplits[0].amountOwed)
        assertEquals(1700L, fiftySplits[1].amountOwed)
        assertEquals(1600L, fiftySplits[2].amountOwed)

        // Subset: 4 of 6 ate (only 4 ids passed)
        val subsetSplits = SettlementEngine.buildEqualSplits(1200L, listOf("m1", "m2", "m3", "m4"))
        assertEquals(4, subsetSplits.size)
        assertEquals(1200L, subsetSplits.sumOf { it.amountOwed })
        assertEquals(300L, subsetSplits[0].amountOwed)
    }

    @Test
    fun testSharesSplit() {
        // 2:1:1 ratio for 2000 paise (20 rupees) (total 4 shares -> 10, 5, 5 rupees = 1000, 500, 500 paise)
        val shares = mapOf("A" to 2, "B" to 1, "C" to 1)
        val splits = SettlementEngine.buildShareSplits(2000L, shares)
        assertEquals(3, splits.size)
        assertEquals(2000L, splits.sumOf { it.amountOwed })
        assertEquals(1000L, splits.find { it.memberId == "A" }?.amountOwed)
        assertEquals(500L, splits.find { it.memberId == "B" }?.amountOwed)
        assertEquals(500L, splits.find { it.memberId == "C" }?.amountOwed)
    }

    @Test
    fun testPercentageSplit() {
        val percentages = mapOf("A" to 50.0, "B" to 25.0, "C" to 25.0)
        val splits = SettlementEngine.buildPercentageSplits(2000L, percentages)
        assertEquals(3, splits.size)
        assertEquals(2000L, splits.sumOf { it.amountOwed })
        assertEquals(1000L, splits.find { it.memberId == "A" }?.amountOwed)
        assertEquals(500L, splits.find { it.memberId == "B" }?.amountOwed)
        assertEquals(500L, splits.find { it.memberId == "C" }?.amountOwed)
    }

    @Test
    fun testSanitizeExpensesAfterMemberRemoval() {
        // A paid 300, split between A and B (150 each). B gets removed.
        val expense = Expense(
            id = "e1",
            tripId = "t1",
            description = "Lunch",
            totalAmount = 300L,
            date = 0L,
            paidBy = listOf(PaidShare("A", 300L)),
            splitAmong = listOf(SplitShare("A", 150L), SplitShare("B", 150L))
        )
        val remainingIds = listOf("A", "C")
        val removed = setOf("B")
        val sanitized = SettlementEngine.sanitizeExpensesAfterMemberRemoval(
            expenses = listOf(expense),
            remainingMemberIds = remainingIds,
            removedIds = removed
        )

        assertEquals(1, sanitized.size)
        val cleaned = sanitized[0]
        // B must NOT be in splitAmong or paidBy
        assert(cleaned.splitAmong.none { it.memberId == "B" })
        assert(cleaned.paidBy.none { it.memberId == "B" })
        assertEquals(300L, cleaned.splitAmong.sumOf { it.amountOwed })
    }

    @Test
    fun testSpendTotalsSeparation() {
        val groupExp = Expense(
            id = "e1",
            tripId = "t1",
            description = "Hotel",
            totalAmount = 5000L,
            date = 0L,
            paidBy = listOf(PaidShare("A", 5000L)),
            splitAmong = listOf(SplitShare("A", 2500L), SplitShare("B", 2500L)),
            isPersonal = false
        )
        val personalExp = Expense(
            id = "e2",
            tripId = "t1",
            description = "Souvenir",
            totalAmount = 1000L,
            date = 0L,
            paidBy = listOf(PaidShare("B", 1000L)),
            splitAmong = emptyList(),
            isPersonal = true,
            personalMemberId = "B"
        )
        val expenses = listOf(groupExp, personalExp)

        assertEquals(5000L, SpendTotals.group(expenses))
        assertEquals(1000L, SpendTotals.personal(expenses))
        assertEquals(5000L, SpendTotals.amountPaid(expenses, "A", groupOnly = true))
        assertEquals(0L, SpendTotals.amountPaid(expenses, "B", groupOnly = true))
        assertEquals(1000L, SpendTotals.amountPaid(expenses, "B", groupOnly = false))
    }
}
