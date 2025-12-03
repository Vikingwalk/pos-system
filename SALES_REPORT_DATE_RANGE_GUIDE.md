# Sales Report Date Range Feature - User Guide

## Overview
The Admin Dashboard Sales Report now supports flexible date range selection, allowing you to generate reports between any two dates with gaps.

## Features

### Report Types
1. **Date Range (Custom)** - Select any start and end date
2. **Weekly** - Report for a specific week
3. **Monthly** - Report for a specific month
4. **All Time** - Complete sales history

## Date Range (Custom) Reports

### How to Use

#### Step 1: Open Sales Report Dialog
- Log in as Admin
- Click the **"Sales Report"** button on the Admin Dashboard

#### Step 2: Select Report Type
- Report Type dropdown defaults to **"Date Range (Custom)"**
- This allows you to specify custom start and end dates

#### Step 3: Choose Date Range
- **From Date**: Select the starting date (defaults to 7 days ago)
- **To Date**: Select the ending date (defaults to today)

**Important Notes:**
- Dates can have gaps (e.g., Jan 1 to Jan 31, or Jan 1 to March 15)
- From Date cannot be after To Date
- Both dates are inclusive (includes sales on both dates)

#### Step 4: Generate Report
- Click **"Generate Report"** button
- Report window opens showing all sales in the date range

### Examples

#### Example 1: Last Week's Sales
```
From Date: December 26, 2024
To Date:   December 3, 2024
Result:    Report shows all sales from Dec 26 to Dec 3 (8 days)
```

#### Example 2: Last Month's Sales
```
From Date: November 1, 2024
To Date:   November 30, 2024
Result:    Report shows all sales for November 2024 (30 days)
```

#### Example 3: Quarter Report with Gap
```
From Date: October 1, 2024
To Date:   December 31, 2024
Result:    Report shows all sales for Oct-Dec 2024 (92 days)
```

#### Example 4: Specific Event Period
```
From Date: December 15, 2024
To Date:   December 20, 2024
Result:    Report shows sales during holiday event (6 days)
```

## Report Information Display

### Report Header
Shows:
- Report Type: "Date Range (Custom)"
- Period: "Oct 01, 2024 to Dec 31, 2024 (92 days)"
- Number of days calculated automatically

### Summary Section
- **Total Transactions**: Count of all sales in range
- **Total Discount**: Sum of all discounts applied
- **Total Sales**: Final revenue (after discounts)

### Detailed Table
Columns:
1. Sale ID
2. Cashier
3. Total
4. Discount
5. Final Amount
6. Payment Method
7. Items (count)
8. Date & Time

## Validation

### Input Validation
✅ **Valid Examples:**
- From: Jan 1, 2024 → To: Jan 31, 2024 (same month)
- From: Jan 1, 2024 → To: Dec 31, 2024 (full year)
- From: Dec 1, 2024 → To: Dec 3, 2024 (3 days)

❌ **Invalid Examples:**
- From: Dec 3, 2024 → To: Dec 1, 2024 (backwards)
  - Error: "From Date cannot be after To Date"
- From: (empty) → To: Dec 3, 2024
  - Error: "Please select both From Date and To Date"

## UI Features

### Date Pickers
- **Calendar Interface**: Click to open calendar popup
- **Manual Entry**: Type date directly (YYYY-MM-DD format)
- **Keyboard Navigation**: Use arrow keys to navigate calendar
- **Prompt Text**: 
  - From Date: "Start date"
  - To Date: "End date"

### Default Values
- **From Date**: 7 days ago (automatically set)
- **To Date**: Today (automatically set)
- Quick way to see last week's sales without changing dates

### Visual Design
- Dark theme matching application
- Bold labels for clarity
- Info text explaining feature
- Color-coded buttons:
  - Green: Generate Report
  - Red: Close

## Use Cases

### Business Analytics
1. **Compare Sales Periods**
   - Generate report for Dec 1-15
   - Generate report for Dec 16-31
   - Compare holiday vs regular periods

2. **Event Performance**
   - Set dates around promotional event
   - Analyze impact on sales

3. **Cashier Performance**
   - Select date range
   - Review sales by cashier in table

4. **Financial Reconciliation**
   - Match date range to accounting period
   - Generate report for tax purposes

### Common Scenarios

#### Scenario 1: Monthly Report
**Goal**: Review November sales
**Steps**:
1. Select "Date Range (Custom)"
2. From Date: November 1, 2024
3. To Date: November 30, 2024
4. Generate Report

#### Scenario 2: Year-to-Date
**Goal**: Sales from January to now
**Steps**:
1. Select "Date Range (Custom)"
2. From Date: January 1, 2024
3. To Date: (keep today's date)
4. Generate Report

#### Scenario 3: Weekend Sales
**Goal**: Sales for last weekend
**Steps**:
1. Select "Date Range (Custom)"
2. From Date: Saturday (e.g., Nov 30)
3. To Date: Sunday (e.g., Dec 1)
4. Generate Report

## Technical Details

### Date Handling
- Dates are inclusive (includes both start and end dates)
- Uses SQL `BETWEEN` clause
- Time component: Includes all sales on the dates (00:00:00 to 23:59:59)
- Timezone: Uses server/database timezone

### SQL Query
```sql
SELECT ... FROM sales s
WHERE DATE(s.created_at) BETWEEN 'from_date' AND 'to_date'
ORDER BY s.created_at DESC
```

### Performance
- Efficient database queries with date indexing
- Large date ranges may take slightly longer
- No limit on date range size

## Troubleshooting

### Issue: No Sales Shown
**Possible Causes:**
- No sales were made in the selected date range
- Check if dates are correct
- Verify sales exist for those dates using "All Time" report

**Solution:**
- Try "All Time" report to see all sales
- Adjust date range

### Issue: "From Date cannot be after To Date" Error
**Cause:** Start date is later than end date

**Solution:**
- Swap the dates
- Or adjust From Date to be earlier

### Issue: Empty Date Fields
**Cause:** Dates not selected

**Solution:**
- Click on date picker calendar icons
- Select valid dates for both fields

### Issue: Report Shows Unexpected Sales
**Cause:** Date range might be wider than expected

**Solution:**
- Double-check From and To dates
- Remember: Both dates are inclusive

## Tips & Best Practices

### For Daily Review
✓ Use default dates (last 7 days)
✓ Quick one-click report generation

### For Monthly Reports
✓ Set From Date to 1st of month
✓ Set To Date to last day of month

### For Comparisons
✓ Generate first report, note the period
✓ Generate second report with different dates
✓ Compare summary sections

### For Accurate Records
✓ Always verify the date range shown in report header
✓ Export or print reports for records
✓ Note the number of days shown in header

## Keyboard Shortcuts

When date picker is focused:
- **Arrow Keys**: Navigate calendar
- **Enter**: Select date
- **Escape**: Close calendar
- **Tab**: Move to next field

In dialog:
- **Tab**: Navigate between fields
- **Enter**: Generate Report (when button focused)
- **Escape**: Close dialog

## Advanced Usage

### Comparing Date Ranges
1. Generate first report and note totals
2. Close report window (stays in background)
3. Change date range in dialog
4. Generate second report
5. Compare side-by-side

### Exporting Data
While report window is open:
1. Select and copy data from table
2. Paste into spreadsheet
3. Or use print functionality (Ctrl+P)

### Custom Periods
- **Fiscal Year**: Set to match your fiscal calendar
- **Quarters**: 3-month periods
- **Bi-weekly**: 14-day periods
- **Custom Events**: Specific promotional dates

## Code Changes Summary

### Modified Files
- `src/controllers/AdminController.java`

### Key Changes
1. **Renamed Report Type**: "Daily" → "Date Range (Custom)"
2. **Enhanced UI**: Added info label explaining feature
3. **Better Defaults**: From Date = 7 days ago, To Date = today
4. **Day Count Display**: Shows number of days in range
5. **Improved Labels**: Bold formatting, clearer text
6. **Better Prompts**: Date pickers have helpful prompt text
7. **Null Handling**: Supports All Time reports without dates

### New Features
- Automatic day count calculation
- Gap-friendly date selection (any range allowed)
- Visual feedback for date range in report header
- Console logging of date range and day count

## Version History

### Version 1.2 (December 2024)
- Enhanced date range UI
- Renamed "Daily" to "Date Range (Custom)"
- Added day count display
- Improved default values (7 days ago to today)
- Better visual styling
- Added info label

### Version 1.1 (November 2024)
- Original date range implementation
- Support for multiple report types

---

## Quick Reference Card

### To Generate Date Range Report:
1. **Open**: Click "Sales Report" button (Admin Dashboard)
2. **Select**: "Date Range (Custom)"
3. **From**: Choose start date (default: 7 days ago)
4. **To**: Choose end date (default: today)
5. **Generate**: Click "Generate Report"

### Common Date Ranges:
- **Last Week**: 7 days ago → today (default)
- **Last Month**: 1st → last day of previous month
- **This Month**: 1st of current month → today
- **Year to Date**: Jan 1 → today
- **Full Year**: Jan 1 → Dec 31

### Report Shows:
- Transaction count
- Total discounts
- Final sales total
- Detailed transaction list
- Cashier information
- Payment methods
- Date and time for each sale

---

**Last Updated**: December 3, 2024  
**Feature Status**: ✅ Enhanced and Ready  
**Testing Status**: Ready for manual testing
