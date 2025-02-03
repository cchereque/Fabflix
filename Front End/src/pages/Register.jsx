import React from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useNavigate} from "react-router-dom"


const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`

const StyledH1 = styled.h1`
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`

const Register = () => {

    const {register, getValues, handleSubmit} = useForm();
    const navigate = useNavigate();

    const submitCredentials = () => {
        const email = getValues("email");
        const password = getValues("password");

        const payLoad = {
            email: email,
            password: password.split('')
        }

        Idm.register(payLoad)
            .then(response => {
                alert(JSON.stringify(response.data, null, 2));
                navigate("/login");
            })
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    return (
        <StyledDiv>
            <h1>Register</h1>
            <input {...register("email")} type={"email"}/>
            <input {...register("password")} type={"password"}/>
            <button onClick={handleSubmit(submitCredentials)}>Register</button>
        </StyledDiv>
    );
}

export default Register;
