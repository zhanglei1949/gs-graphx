/* Copyright 2020 Alibaba Group Holding Limited. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#ifndef GRAPHLEARN_CORE_DAG_OPTIMIZER_H_
#define GRAPHLEARN_CORE_DAG_OPTIMIZER_H_

#include <string>
#include "core/dag/dag.h"

namespace graphlearn {

class Optimizer {
public:
  Optimizer() = default;
  ~Optimizer() = default;

  virtual bool Optimize(Dag* dag) {
    return true;
  }

private:
  Optimizer(const Optimizer&) = delete;
  Optimizer& operator=(const Optimizer&) = delete;
};

}  // namespace graphlearn

#endif  // GRAPHLEARN_CORE_DAG_OPTIMIZER_H_
