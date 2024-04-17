package com.alibaba.graphscope.arrow.array;

import com.alibaba.fastffi.CXXHead;
import com.alibaba.fastffi.CXXValue;
import com.alibaba.fastffi.FFIFactory;
import com.alibaba.fastffi.FFIGen;
import com.alibaba.fastffi.FFINameAlias;
import com.alibaba.fastffi.FFISettablePointer;
import com.alibaba.fastffi.FFITypeAlias;
import com.alibaba.graphscope.arrow.Status;

import static com.alibaba.graphscope.utils.CppClassName.GS_BASE_ARROW_ARRAY_BUILDER;
import static com.alibaba.graphscope.utils.CppHeaderName.CORE_JAVA_TYPE_ALIAS_H;

@FFIGen
@CXXHead(CORE_JAVA_TYPE_ALIAS_H)
@FFITypeAlias(GS_BASE_ARROW_ARRAY_BUILDER)
public interface BaseArrowArrayBuilder<T> extends FFISettablePointer {
    @FFINameAlias("Reserve")
    @CXXValue
    Status reserve(long additionalCapacity);

    @FFIFactory
    interface Factory<T> {

        BaseArrowArrayBuilder<T> create();
    }
}
