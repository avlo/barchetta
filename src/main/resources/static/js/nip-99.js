let dateNow;

$(document).ready(function () {
    $('#createContract').on('submit', async function (event) {
        event.preventDefault();
        dateNow = Math.floor(Date.now() / 1000);
        await createEventRxR(await generateCTBEventJson());
        console.log(contractDto);
        $.ajax({
            type: 'POST',
            url: '/contract/create',
            data: contractDto,
            // contentType: 'application/json',
            // data: JSON.stringify(product),
            // success: function (response) {
            //     alert('Product added: ' + response.name);
            //     location.reload();
            // },
            error: function (error) {
                alert('Error adding product');
            }
        });
    });
});

async function generateCTBEventJson() {
    let $content = $("#content");
    return {
        id: '',
        kind: 31923,
        created_at: dateNow,
        content: ("CTBEvent content field: " + $content.val()),
        tags: [
            ['subject', "CTBEvent subject field: " + $content.val()],
            ['title', "CTBEvent title field: " + $content.val()],
            ['published_at', dateNow],
            ['summary', "CTBEvent summary field: " + $content.val()],
            ['location', "CTBEvent location field"],
            ['p', await window.nostr.getPublicKey(), "wss://localhost:5555", $("#role").val()]
            // ['p', "9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224", "wss://localhost:5555", $("#role").val()]
        ],
        pubkey: '',
        sig: ''
    }
}

async function generateCLEventJson(ctbEventId) {
    let $content = $("#content");
    return {
        id: '',
        kind: 30402,
        created_at: dateNow,
        content: "CLEvent content field: " + $content.val(),
        tags: [
            ['subject', "CLEvent subject field: " + $content.val()],
            ['title', "CLEvent title field: " + $content.val()],
            ['published_at', dateNow],
            ['summary', "CLEvent summary field: " + $content.val()],
            ['location', "CLEvent location field"],
            ['price', $("#payoutAmount").val(), "BTC", "1"],
            ['p', await window.nostr.getPublicKey(), "ws://localhost:5555", $("#role").val()],
            // ['p', "111df01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f", "ws://localhost:5555", $("#role").val()]
            ['a', "31923: " + await window.nostr.getPublicKey() + ":" + ctbEventId]
        ],
        pubkey: '',
        sig: ''
    }
}

async function createEventRxR(generatedCTBEventJson) {
    await signEvent(generatedCTBEventJson)
        .then(signedCtbEventJson =>
            calendarTimeBasedEventDto = $.extend(calendarTimeBasedEventDto, signedCtbEventJson));

    classifiedListingEventDto = $.extend(
        classifiedListingEventDto,
        await signEvent(
            await generateCLEventJson(calendarTimeBasedEventDto.id)));

    contractDto.setAttribute("classifiedListingEventDto", classifiedListingEventDto);
    contractDto.setAttribute("calendarTimeBasedEventDto", calendarTimeBasedEventDto);
}

async function signEvent(event) {
    console.log('signEvent() input: \n\n' + event + '\n\n');
    const signedPopulatedEvent = await window.nostr.signEvent(event);
    console.log('signEvent() output: ' + signedPopulatedEvent);
    return signedPopulatedEvent;
}

function sendEvent() {
    console.log("******************");
    console.log("******************");
    console.log("calendarTimeBasedEventDto: ", calendarTimeBasedEventDto);
    console.log("-------------");
    console.log("classifiedListingEventDto: ", classifiedListingEventDto);
    console.log("=============");
    console.log("contractDto: ", contractDto);
    console.log("******************");
    console.log("******************");
    const form = document.querySelector('form');
    // let formData = new FormData(contractDto);
    form.submit();
}

async function sendData() {
    contractDto.setAttribute("classifiedListingEventDto", classifiedListingEventDto);
    contractDto.setAttribute("calendarTimeBasedEventDto", calendarTimeBasedEventDto);
    const form = document.querySelector('form');
    const formData = new FormData(form);
    const response = await fetch("contract/create", {
        method: "POST",
        // Set the FormData instance as the request body
        body: formData,
    });
    console.log(await response.json());
}
