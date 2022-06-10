module.exports = ({ context, core, results }) => {
    const currentBranch = context.ref.split('heads/')[1];
    const commentTemplate = {
        "@type": "ActionCard",
        "@context": "http://schema.org/extensions",
        "themeColor": "0076D7",
        "summary": "Code coverage report",
        "sections": [{
            "activityTitle": `Code coverage report - ${context.repo.repo}`,
            "activitySubtitle": currentBranch,
            "activityImage": context.payload.sender.avatar_url,
            "facts": [{
                "name": "Lines:",
                "value": `${results.lines}%`
            }, {
                "name": "Statements:",
                "value": `${results.statements}%`
            }, {
                "name": "Functions:",
                "value": `${results.functions}%`
            },{
                "name": "Branches:",
                "value": `${results.branches}%`
            }],
            "markdown": true
        }],
        "potentialAction": [
            {
                "@type": "OpenUri",
                "name": "View report",
                "targets": [
                    {
                        "os": "default",
                        "uri": `${context.payload.repository.url}/actions/runs/${context.runId}`
                    }
                ]
            },
            {
                "@type": "OpenUri",
                "name": "View repository",
                "targets": [
                    {
                        "os": "default",
                        "uri": context.payload.repository.url
                    }
                ]
            },
            {
                "@type": "OpenUri",
                "name": "View branch",
                "targets": [
                    {
                        "os": "default",
                        "uri": `${context.payload.repository.url}/tree/${currentBranch}`
                    }
                ]
            },
            {
                "@type": "OpenUri",
                "name": "View commit",
                "targets": [
                    {
                        "os": "default",
                        "uri": context.payload.head_commit.url
                    }
                ]
            }
        ]
    };
    core.setOutput('comment', commentTemplate);
    return commentTemplate;
};