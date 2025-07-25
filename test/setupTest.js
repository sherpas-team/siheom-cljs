import "@testing-library/jest-dom";
import { getA11ySnapshot } from "./siheom/getA11ySnapshot.js";

if (typeof global !== "undefined") {
	await import('vitest-canvas-mock');
}

const koreanRegex = /[\u1100-\u11FF\u3130-\u318F\uA960-\uA97F\uAC00-\uD7AF\uD7B0-\uD7FF]/g;

function calculateTextWidth(text) {
	const koreanCount = (text?.match(koreanRegex) || []).length;
	const totalCount = text.length + koreanCount;
	return totalCount;
}

function renderRowsToMarkdown(cellContents) {
	// Calculate max width for each column
	const columnWidths = cellContents[0].map((_, colIndex) =>
		Math.max(...cellContents.map(row => calculateTextWidth(row[colIndex] ?? "")))
	);

	// Pad cells and create markdown
	let temp = [];

	cellContents.forEach((row, rowIndex) => {
		const paddedRow = row.map((cell, cellIndex) => {
			const contentWidth = calculateTextWidth(cell)
			const columnWidth = columnWidths[cellIndex]

			return ' '.repeat(columnWidth - contentWidth) + cell
		});

		temp.push(`| ${paddedRow.join(' | ')} |\n`);

		// Add separator after header
		if (rowIndex === 0) {
			temp.push(`| ${columnWidths.map(width => '-'.repeat(width)).join(' | ')} |\n`);
		}
	});

	return temp.join('');
}

window.renderRowsToMarkdown = renderRowsToMarkdown;

window.tableToMarkdown = function tableToMarkdown(tableElement) {
	// Get all rows including header
	const allRows = [
		...Array.from(tableElement.querySelectorAll(':is(thead, [aria-roledescription="tableheader"]) :is(tr, [role="row"])')),
		...Array.from(tableElement.querySelectorAll(':is(tbody, [aria-roledescription="tablebody"]) :is(tr, [role="row"])'))
	];

	// Extract cell contents
	const cellContents = allRows.map(row =>
		Array.from(row.querySelectorAll('th, td, [role="cell"], [role="columnheader"]')).map(cell => {

			const cellInput = cell.querySelector('input, progress');

			return cellInput ? String(cellInput.value) : cell.textContent?.trim() ?? ''
		})
	);

	return renderRowsToMarkdown(cellContents);
}
window.getA11ySnapshot = getA11ySnapshot