const fs = require('fs');

const calculatePercentage = (result) => {
    const [, missed, covered] = result.replace(/[a-zA-Z-="]/g, '').split(' ');
    return ((Number(covered) * 100) / (Number(missed) + Number(covered))).toFixed(2);
};

const getXmlSummary = (xml) => {
    return xml.split('</package><counter ')[1]
       .replace(/\/><counter /g, '')
       .replace(/\/><\/report>/, '')
       .split('\"t');
};

module.exports = ({ core }) => {
    const result = fs.readFileSync('target/site/jacoco/jacoco.xml', 'utf8')
    const summary = getXmlSummary(result);

    const lines = calculatePercentage(summary[2]);
    const statements = calculatePercentage(summary[0]);
    const functions = calculatePercentage(summary[4]);
    const branches = calculatePercentage(summary[1]);
    const results = `| Lines | Statements | Functions | Branches |\n| ------ | ------ | ------ | ------ |\n| ${lines}% | ${statements}% | ${functions}% | ${branches}% |`;

    console.log('✅ Code coverage results:\n', results);
    core.setOutput('stringResults', results);
    core.setOutput('objectResults', { lines, statements, functions, branches });
    return results;
};