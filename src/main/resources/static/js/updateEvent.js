let dateNow;

$(document).ready(function () {
    $("#updateContract").submit(function (e) {
        e.preventDefault();
        const form = this;
        createUpdatedEvent().then(function () {
            form.submit();
        })
    });
});

async function createUpdatedEvent() {
    dateNow = Math.floor(Date.now() / 1000);

    const rsvpEventJson =
        await signRsvpEvent(
            await generateRsvpEventJson());

    document.getElementById('rsvpEventJson').value = JSON.stringify(rsvpEventJson);
}

async function generateRsvpEventJson() {
    const content = $("#content");
    return {
        id: '',
        kind: 31925,
        created_at: dateNow,
        content: "RsvpEvent content field: " + content.val(),
        tags: [
            // required tags
            ['d', $("#eventUuid").val()],
            ['a', "31923:" + await window.nostr.getPublicKey() + ":" + $("#eventUuid").val()],
            // barchetta tags
            ["status", "<accepted/declined/tentative>"],
            ["i", "<approve/veto>"]
        ],
        pubkey: '',
        sig: ''
    }
}

async function signRsvpEvent(event) {
    console.log('signEvent() input: \n\n' + event + '\n\n');
    const signedPopulatedEvent = await window.nostr.signEvent(event);
    console.log('signEvent() output: ' + signedPopulatedEvent);
    return signedPopulatedEvent;
}
