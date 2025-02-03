import Config from "backend/config.json";
import Axios from "axios";


/**
 * We use axios to create REST calls to our backend
 *
 * We have provided the login rest call for your
 * reference to build other rest calls with.
 *
 * This is an async function. Which means calling this function requires that
 * you "chain" it with a .then() function call.
 * <br>
 * What this means is when the function is called it will essentially do it "in
 * another thread" and when the action is done being executed it will do
 * whatever the logic in your ".then()" function you chained to it
 * @example
 * login(request)
 * .then(response => alert(JSON.stringify(response.data, null, 2)));
 */
async function login(loginRequest) {
    const requestBody = {
        email: loginRequest.email,
        password: loginRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.login, // Path of URL ("/login")
        data: requestBody // Data to send in Body (The RequestBody to send)
    }

    return Axios.request(options);
}

async function register(registerRequest) {
    const requestBody = {
        email: registerRequest.email,
        password: registerRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.register, // Path of URL ("/login")
        data: requestBody // Data to send in Body (The RequestBody to send)
    }

    return Axios.request(options);
}

async function movieSearch(movieSearchRequest, accessToken) {
    const queryParams = {
        title: movieSearchRequest.title,
        year: movieSearchRequest.year,
        director: movieSearchRequest.director,
        genre: movieSearchRequest.genre,
        orderBy: movieSearchRequest.orderBy,
        direction: movieSearchRequest.direction,
        limit: movieSearchRequest.limit,
        page: movieSearchRequest.page
    };

    const options = {
        method: "GET",
        baseURL: Config.baseURL,
        url: Config.movie.search,
        params: queryParams,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}

async function movieDetails(movieId, accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.baseURL,
        url: "/movie/" + movieId,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    };

    return Axios.request(options);
}

async function cartInsert(payload, accessToken) {
    const requestBody = {
        movieId: payload.movieId,
        quantity: payload.quantity
    }

    const options = {
        method: "POST",
        baseURL: Config.billingUrl,
        url: Config.billing.addToCart,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}

async function retrieveCart(accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.billingUrl,
        url: Config.billing.retrieveCart,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}

async function deleteItem(id, accessToken) {
    console.log("assigning options");
    const options = {
        method: "DELETE",
        baseURL: Config.billingUrl,
        url: Config.billing.removeItem + id,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }
    console.log("assigned");
    return Axios.request(options);
}

async function updateCartItem(payload, accessToken) {
    const requestBody = {
        movieId: payload.id,
        quantity: payload.quantity
    }

    const options = {
        method: "POST",
        baseURL: Config.billingUrl,
        url: Config.billing.updateItem,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }
    return Axios.request(options);
}

async function getOrderHistory(accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.billingUrl,
        url: Config.billing.orderHistory,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}

async function initiatePayment(accessToken) {
    const options = {
        method: "GET",
        baseURL: Config.billingUrl,
        url: Config.billing.orderPayment,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}

async function completeOrder(paymentId, accessToken) {
    const requestBody = {
        paymentIntentId: paymentId
    }

    const options = {
        method: "POST",
        baseURL: Config.billingUrl,
        url: Config.billing.orderComplete,
        data: requestBody,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options)
}

export default {
    login,
    register,
    movieSearch,
    movieDetails,
    cartInsert,
    retrieveCart,
    deleteItem,
    updateCartItem,
    getOrderHistory,
    initiatePayment,
    completeOrder
}
