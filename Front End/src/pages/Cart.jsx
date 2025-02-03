import React, {useEffect} from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useNavigate} from "react-router-dom"
import { loadStripe } from "@stripe/stripe-js";
import { Elements } from "@stripe/react-stripe-js";
import CheckoutForm from "./CheckoutForm";


const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`
const StyledDiv2 = styled.div`
  display: flex;
  flex-direction: row;
`

const StyledTd = styled.td`
    border: 1px solid black;
    text-align: left;
    padding: 15px
`


const StyledTh = styled.th`
    text-align: left;
    padding-left: 10px;
    padding=right: 10px;
`

const StyledTr = styled.tr`
    border: 1px solid black;
    padding: 25px;
`

const StyledTable = styled.table`
    text-align: left;
    border: 1px solid black;
    width: 100%;
    padding: 3px
`

const StyledP = styled.p`
    text-align: right;
`

const StyledH1 = styled.h1`
    font-size: 36px;
    text-align: center;
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
    width: 50px;
`
const StyledButton1 = styled.button`
    width: 100px;
`

const stripePromise = loadStripe("pk_test_51KxH5NCbD1PK386NIV90MBrOyBNruofJaYtTSdJU6Mj0Kmhr8AbFep6GiKWTad6kWAtrP0ZU2qOiI8HdSNfeLzIK00DTJcKHnc");


const Cart = () =>
{
    const [items, setItems] = React.useState([]);
    const [total, setTotal] = React.useState([]);
    const [clientSecret, setClientSecret] = React.useState("");
    const [paymentIntentId, setPaymentIntentId] = React.useState("");
    const {register, getValues} = useForm();
    const navigate = useNavigate();

    const getCart = () => {
        Idm.retrieveCart(localStorage.getItem("access_token"))
            .then(response => {
                setItems(response.data["items"]);
                setTotal(response.data["total"]);
            })
    }

    const removeItem = (id) => {
        Idm.deleteItem(id, localStorage.getItem("access_token")).then(response => getCart())
    }

    const updateItemQuantity = (id, quantity) => {
        if (quantity !== "") {
            const payload = {
                id: id,
                quantity: quantity
            }
            console.log(payload.id);
            console.log(payload.quantity);
            Idm.updateCartItem(payload, localStorage.getItem("access_token")).then(response => getCart())
        }
    }

    const initiateCheckout = () => {
        console.log("Initiated Checkout");
        Idm.initiatePayment(localStorage.getItem("access_token")).then(response => {
                setClientSecret(response.data["clientSecret"]);
                setPaymentIntentId(response.data["paymentIntentI"]);
            }
        )
    }

    const appearance = {
        theme: 'stripe'
    }

    const options = {
        clientSecret,
        appearance,
    };

    useEffect(() => {
        getCart();
    },[])

    return (
        <StyledDiv>
            <StyledH1>Cart</StyledH1>
            <br></br>
            <br></br>
            {items &&
                <StyledTable>
                    <StyledTr>
                        <StyledTh>Title</StyledTh>
                        <StyledTh>Quantity</StyledTh>
                        <StyledTh>Price</StyledTh>
                        <StyledTh></StyledTh>
                    </StyledTr>
                    {items.map(item =>
                        <StyledTr>
                            <StyledTd>{item.movieTitle}</StyledTd>
                            <StyledTd>
                                <p>{item.quantity}</p>
                                <p>
                                    <select {...register("newQuantity")}>
                                        <option value=""> </option>
                                        <option value={1}>1</option>
                                        <option value={2}>2</option>
                                        <option value={3}>3</option>
                                        <option value={4}>4</option>
                                        <option value={5}>5</option>
                                        <option value={6}>6</option>
                                        <option value={7}>7</option>
                                        <option value={8}>8</option>
                                        <option value={9}>9</option>
                                        <option value={10}>10</option>
                                </select>
                                    <StyledButton onClick={() => updateItemQuantity(item.movieId, getValues("newQuantity"))}>update</StyledButton>
                                </p>
                            </StyledTd>
                            <StyledTd>{(item.quantity * item.unitPrice).toFixed(2)}</StyledTd>
                            <StyledTd><StyledButton onClick={() => removeItem(item.movieId)}>Delete</StyledButton></StyledTd>
                        </StyledTr>
                        )
                    }
                </StyledTable>
            }
            <br></br>
            {items &&
                <StyledDiv2>
                    <StyledP>Total:    </StyledP>
                    <StyledP>   {total}</StyledP>
                </StyledDiv2>
            }
            <br></br>
            {items &&
                <StyledDiv2>
                    <StyledButton1 onClick={() => initiateCheckout()}>Checkout</StyledButton1>
                </StyledDiv2>
            }

            {clientSecret && (
                    <Elements options={options} stripe={stripePromise}>
                        <CheckoutForm />
                    </Elements>
                )

            }
            <br></br>
            <StyledButton1 onClick={() => navigate("/orderhistory")}>Order History</StyledButton1>
        </StyledDiv>
    )
}

export default Cart;