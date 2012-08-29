/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.reflect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.annotation.Nullable;

/**
 * Wrapper around either a {@link Method} or a {@link Constructor}.
 * Convenience API is provided to make common reflective operation easier to deal with,
 * such as {@link #isPublic}, {@link #getParameters} etc.
 * 
 * @param <T> the type that declares this method or constructor.
 * @param <R> the return type of (or supertype thereof) the method or the declaring type of the
 *            constructor.
 * @author Ben Yu
 * @since 14.0
 */
@Beta
public abstract class Invokable<T, R> extends Element implements GenericDeclaration {

  <M extends AccessibleObject & Member> Invokable(M member) {
    super(member);
  }

  /**
   * Returns {@link Invokable} of {@code method}. Note that even though the returned type is
   * {@code Invokable<Object, Object>}, it's not statically type safe to pass any arbitrary {@code
   * Object} to the {@link #invoke} method. Runtime type check will be performed by the underlying
   * {@code method}.
   */
  public static Invokable<Object, Object> from(Method method) {
    return new MethodInvokable<Object>(method);
  }

  /** Returns {@link Invokable} of {@code constructor}. */
  public static <T> Invokable<T, T> from(Constructor<T> constructor) {
    return new ConstructorInvokable<T>(constructor);
  }

  /**
   * Returns {@code true} if this is an overridable method; {@code false} if either it's not
   * overridable, or if it's a constructor.
   */
  public abstract boolean isOverridable();

  /**
   * Invokes with {@code receiver} as the 'this' and {@code args} passed to the underlying method
   * and returns the return value; or calls the underlying constructor with {@code args} and returns
   * the constructed instance.
   *
   * @throws IllegalAccessException if this {@code Constructor} object enforces Java language
   *         access control and the underlying method or constructor is inaccessible.
   * @throws IllegalArgumentException if the number of actual and formal parameters differ;
   *         if an unwrapping conversion for primitive arguments fails; or if, after possible
   *         unwrapping, a parameter value cannot be converted to the corresponding formal
   *         parameter type by a method invocation conversion.
   * @throws InvocationTargetException if the underlying method or constructor throws an exception.
   */
  // All subclasses are owned by us and we'll make sure to get the R type right.
  @SuppressWarnings("unchecked")
  public final R invoke(@Nullable T receiver, Object... args)
      throws InvocationTargetException, IllegalAccessException {
    return (R) invokeInternal(receiver, checkNotNull(args));
  }

  /** Returns the return type of this delegate. */
  // All subclasses are owned by us and we'll make sure to get the R type right.
  @SuppressWarnings("unchecked")
  public final TypeToken<? extends R> getReturnType() {
    return (TypeToken<? extends R>) TypeToken.of(getGenericReturnType());
  }

  /** Returns all declared parameters of this delegate. */
  public final ImmutableList<Parameter<?>> getParameters() {
    Type[] parameterTypes = getGenericParameterTypes();
    Annotation[][] annotations = getParameterAnnotations();
    ImmutableList.Builder<Parameter<?>> builder = ImmutableList.builder();
    for (int i = 0; i < parameterTypes.length; i++) {
      builder.add(new Parameter<Object>(
          this, i, TypeToken.of(parameterTypes[i]), annotations[i]));
    }
    return builder.build();
  }

  /** Returns all declared exception types of this delegate. */
  public final ImmutableList<TypeToken<? extends Throwable>> getExceptionTypes() {
    ImmutableList.Builder<TypeToken<? extends Throwable>> builder = ImmutableList.builder();
    for (Type type : getGenericExceptionTypes()) {
       // getGenericExceptionTypes() will never return a type that's not exception
      @SuppressWarnings("unchecked")
      TypeToken<? extends Throwable> exceptionType = (TypeToken<? extends Throwable>)
          TypeToken.of(type);
      builder.add(exceptionType);
    }
    return builder.build();
  }

  /**
   * Explicitly specifies the {@code returnType return type} of the functor. For example:
   * <pre>   {@code
   *   Method factoryMethod = Person.class.getMethod("create");
   *   Invokable<?, Person> factory = Invokable.of(getNameMethod).returning(Person.class);
   * }</pre>
   */
  public final <R1 extends R> Invokable<T, R1> returning(Class<R1> returnType) {
    return returning(TypeToken.of(returnType));
  }

  /** Explicitly specifies the {@code returnType return type} of the functor. */
  public final <R1 extends R> Invokable<T, R1> returning(TypeToken<R1> returnType) {
    if (!returnType.isAssignableFrom(getReturnType())) {
      throw new IllegalArgumentException(
          "Invokable is known to return " + getReturnType() + ", not " + returnType);
    }
    @SuppressWarnings("unchecked") // guarded by previous check
    Invokable<T, R1> specialized = (Invokable<T, R1>) this;
    return specialized;
  }

  abstract Object invokeInternal(Object receiver, Object[] args)
      throws InvocationTargetException, IllegalAccessException;

  abstract Type[] getGenericParameterTypes();

  /** This should never return a type that's not a subtype of Throwable. */
  abstract Type[] getGenericExceptionTypes();

  abstract Annotation[][] getParameterAnnotations();

  abstract Type getGenericReturnType();
  
  private static class MethodInvokable<T> extends Invokable<T, Object> {

    private final Method method;

    MethodInvokable(Method method) {
      super(method);
      this.method = method;
    }

    @Override Object invokeInternal(Object receiver, Object[] args)
        throws InvocationTargetException, IllegalAccessException {
      return method.invoke(receiver, args);
    }

    @Override Type getGenericReturnType() {
      return method.getGenericReturnType();
    }

    @Override Type[] getGenericParameterTypes() {
      return method.getGenericParameterTypes();
    }

    @Override Type[] getGenericExceptionTypes() {
      return method.getGenericExceptionTypes();
    }

    @Override Annotation[][] getParameterAnnotations() {
      return method.getParameterAnnotations();
    }

    @Override public final TypeVariable<?>[] getTypeParameters() {
      return method.getTypeParameters();
    }

    @Override public final boolean isOverridable() {
      return  !(isFinal() || isPrivate() || isStatic()
          || Modifier.isFinal(getDeclaringClass().getModifiers()));
    }
  }

  private static class ConstructorInvokable<T> extends Invokable<T, T> {

    private final Constructor<?> constructor;

    ConstructorInvokable(Constructor<?> constructor) {
      super(constructor);
      this.constructor = constructor;
    }

    @Override Object invokeInternal(Object receiver, Object[] args)
        throws InvocationTargetException, IllegalAccessException {
      try {
        return constructor.newInstance(args);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    }

    @Override Type getGenericReturnType() {
      return constructor.getDeclaringClass();
    }

    @Override Type[] getGenericParameterTypes() {
      return constructor.getGenericParameterTypes();
    }

    @Override Type[] getGenericExceptionTypes() {
      return constructor.getGenericExceptionTypes();
    }

    @Override Annotation[][] getParameterAnnotations() {
      return constructor.getParameterAnnotations();
    }

    @Override public final TypeVariable<?>[] getTypeParameters() {
      return constructor.getTypeParameters();
    }

    @Override public final boolean isOverridable() {
      return false;
    }
  }
}
